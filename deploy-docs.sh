#!/bin/bash
set -e

echo "📚 Kafka Training Documentation Deployment"
echo "=========================================="
echo ""

# Check if mkdocs is installed
if ! command -v mkdocs &> /dev/null; then
    echo "❌ Error: mkdocs not found"
    echo "   Install with: pip install -r requirements.txt"
    exit 1
fi

# Check if wrangler is installed
if ! command -v wrangler &> /dev/null; then
    echo "❌ Error: wrangler not found"
    echo "   Install with: npm install -g wrangler"
    exit 1
fi

echo "🔨 Building MkDocs site..."
mkdocs build

if [ ! -d "site" ]; then
    echo "❌ Error: site directory not found after build"
    exit 1
fi

echo "✅ MkDocs site built successfully"
echo ""

echo "🚀 Deploying to Cloudflare Workers..."
wrangler deploy

echo ""
echo "✅ Deployment complete!"
echo ""
echo "📊 Next steps:"
echo "   - View your site at the URL shown above"
echo "   - Check logs: wrangler tail"
echo "   - View analytics in Cloudflare dashboard"
echo ""
