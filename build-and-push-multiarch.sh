#!/bin/bash

# Build and push multi-architecture Docker images for linter-rules-for-nextflow
# Supports both arm64 and amd64 architectures
# Compatible with both Docker and Finch

set -e

# Configuration
REGISTRY="public.ecr.aws/aws-genomics"
IMAGE_NAME="linter-rules-for-nextflow"
VERSION="v0.1.2"
PLATFORMS=("linux/amd64" "linux/arm64")

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Detect container runtime
CONTAINER_RUNTIME=""
if command -v finch > /dev/null 2>&1; then
    CONTAINER_RUNTIME="finch"
    print_status "Using Finch as container runtime"
elif command -v docker > /dev/null 2>&1; then
    CONTAINER_RUNTIME="docker"
    print_status "Using Docker as container runtime"
else
    print_error "Neither Finch nor Docker found. Please install one of them."
    exit 1
fi

# Check if AWS CLI is available and configured
if ! aws --version > /dev/null 2>&1; then
    print_error "AWS CLI is not installed or not in PATH."
    exit 1
fi

print_status "Starting multi-architecture build and push process..."

# Authenticate with ECR Public
print_status "Authenticating with ECR Public..."
aws ecr-public get-login-password --region us-east-1 | $CONTAINER_RUNTIME login --username AWS --password-stdin public.ecr.aws

# Function to build with Docker buildx (multi-platform in single command)
build_with_docker() {
    # Check if buildx is available
    if ! docker buildx version > /dev/null 2>&1; then
        print_error "Docker buildx is not available. Please ensure you have Docker Desktop or buildx plugin installed."
        exit 1
    fi

    # Create a new builder instance if it doesn't exist
    BUILDER_NAME="multiarch-builder"
    if ! docker buildx ls | grep -q "$BUILDER_NAME"; then
        print_status "Creating new buildx builder instance: $BUILDER_NAME"
        docker buildx create --name "$BUILDER_NAME" --driver docker-container --bootstrap
    fi

    # Use the builder
    print_status "Using buildx builder: $BUILDER_NAME"
    docker buildx use "$BUILDER_NAME"

    # Build and push for both tags
    TAGS=(
        "${REGISTRY}/${IMAGE_NAME}:latest"
        "${REGISTRY}/${IMAGE_NAME}:${VERSION}"
    )

    for TAG in "${TAGS[@]}"; do
        print_status "Building and pushing: $TAG"
        print_status "Platforms: linux/amd64,linux/arm64"
        
        docker buildx build \
            --platform linux/amd64,linux/arm64 \
            --tag "$TAG" \
            --push \
            .
        
        if [ $? -eq 0 ]; then
            print_status "Successfully built and pushed: $TAG"
        else
            print_error "Failed to build and push: $TAG"
            exit 1
        fi
    done
}

# Function to build with Finch (separate builds per platform)
build_with_finch() {
    print_warning "Finch requires separate builds for each platform"
    
    # Tags to build
    TAGS=(
        "${REGISTRY}/${IMAGE_NAME}:latest"
        "${REGISTRY}/${IMAGE_NAME}:${VERSION}"
    )
    
    # Build for each platform and tag combination
    for PLATFORM in "${PLATFORMS[@]}"; do
        ARCH=$(echo $PLATFORM | cut -d'/' -f2)
        print_status "Building for platform: $PLATFORM"
        
        for TAG in "${TAGS[@]}"; do
            PLATFORM_TAG="${TAG}-${ARCH}"
            print_status "Building: $PLATFORM_TAG"
            
            finch builder build \
                --platform "$PLATFORM" \
                --tag "$PLATFORM_TAG" \
                .
            
            if [ $? -ne 0 ]; then
                print_error "Failed to build: $PLATFORM_TAG"
                exit 1
            fi
            
            print_status "Pushing: $PLATFORM_TAG"
            finch push "$PLATFORM_TAG"
            
            if [ $? -ne 0 ]; then
                print_error "Failed to push: $PLATFORM_TAG"
                exit 1
            fi
        done
    done
    
    # Create and push multi-arch manifests
    print_status "Creating multi-architecture manifests..."
    
    for TAG in "${TAGS[@]}"; do
        print_status "Creating manifest for: $TAG"
        
        # Create manifest list
        MANIFEST_ARGS=()
        for PLATFORM in "${PLATFORMS[@]}"; do
            ARCH=$(echo $PLATFORM | cut -d'/' -f2)
            MANIFEST_ARGS+=("${TAG}-${ARCH}")
        done
        
        # Note: Finch doesn't have native manifest support, so we'll use Docker for this part
        if command -v docker > /dev/null 2>&1; then
            print_status "Using Docker to create manifest (Finch limitation)"
            docker manifest create "$TAG" "${MANIFEST_ARGS[@]}"
            docker manifest push "$TAG"
        else
            print_warning "Cannot create multi-arch manifest without Docker. Individual platform images pushed:"
            for PLATFORM in "${PLATFORMS[@]}"; do
                ARCH=$(echo $PLATFORM | cut -d'/' -f2)
                echo "  - ${TAG}-${ARCH}"
            done
        fi
    done
}

# Execute build based on container runtime
if [ "$CONTAINER_RUNTIME" = "docker" ]; then
    build_with_docker
else
    build_with_finch
fi

print_status "Multi-architecture build and push completed successfully!"
print_status "Images pushed:"
TAGS=(
    "${REGISTRY}/${IMAGE_NAME}:latest"
    "${REGISTRY}/${IMAGE_NAME}:${VERSION}"
)
for TAG in "${TAGS[@]}"; do
    echo "  - $TAG"
done

if [ "$CONTAINER_RUNTIME" = "docker" ]; then
    print_status "You can verify the images with:"
    echo "  docker buildx imagetools inspect ${REGISTRY}/${IMAGE_NAME}:latest"
    echo "  docker buildx imagetools inspect ${REGISTRY}/${IMAGE_NAME}:${VERSION}"
else
    print_status "Individual platform images were pushed. If Docker is available, multi-arch manifests were created."
fi