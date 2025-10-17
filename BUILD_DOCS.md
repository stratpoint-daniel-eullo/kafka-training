# Building and Deploying Documentation

This guide explains how to build and deploy the Kafka Training MkDocs Material documentation.

## Quick Start

### 1. Install Dependencies

```bash
# Install Python dependencies
pip install -r requirements.txt

# Verify installation
mkdocs --version
# Expected: mkdocs, version 1.5.x or higher
```

### 2. Local Development

```bash
# Start live-reloading server
mkdocs serve

# Access documentation at:
# http://localhost:8000

# The server automatically reloads when you save changes
```

### 3. Build Static Site

```bash
# Build documentation
mkdocs build

# Output directory: site/
# Files ready for deployment
```

## Detailed Setup

### Prerequisites

**Required:**

- Python 3.8 or higher
- pip (Python package manager)

**Verify:**

```bash
python3 --version
# Python 3.8.x or higher

pip3 --version
# pip 21.x or higher
```

### Install Python (if needed)

=== "macOS"

    ```bash
    # Using Homebrew
    brew install python3

    # Verify
    python3 --version
    ```

=== "Ubuntu/Debian"

    ```bash
    # Install Python 3
    sudo apt update
    sudo apt install python3 python3-pip

    # Verify
    python3 --version
    ```

=== "Windows"

    1. Download from [python.org](https://www.python.org/downloads/)
    2. Run installer (check "Add Python to PATH")
    3. Verify: `python --version`

### Install MkDocs Material

**Option 1: Install from requirements.txt (Recommended)**

```bash
pip install -r requirements.txt
```

**Option 2: Install manually**

```bash
pip install mkdocs
pip install mkdocs-material
pip install pymdown-extensions
```

**Verify Installation:**

```bash
mkdocs --version
# mkdocs, version 1.5.x

pip list | grep mkdocs
# mkdocs                    1.5.x
# mkdocs-material           9.5.x
# mkdocs-material-extensions 1.3.x
```

## Development Workflow

### Start Development Server

```bash
# Default (localhost:8000)
mkdocs serve

# Custom address
mkdocs serve -a localhost:8001

# Custom address and port
mkdocs serve -a 0.0.0.0:8080

# With live reload
mkdocs serve --livereload
```

Access at: http://localhost:8000

**Features:**

- Live reload on file changes
- Automatic browser refresh
- Error reporting in browser
- Fast build times

### Edit Documentation

1. Edit files in `docs/` directory
2. Save changes
3. Browser automatically refreshes
4. Verify changes immediately

**File Structure:**

```
docs/
├── index.md                # Homepage
├── getting-started/       # Setup guides
├── training/              # Training curriculum
├── containers/            # Container guides
├── deployment/            # Deployment guides
├── api/                   # API reference
├── architecture/          # Architecture docs
├── contributing/          # Contribution guides
├── stylesheets/          # Custom CSS
└── javascripts/          # Custom JS
```

### Add New Pages

1. Create markdown file in appropriate directory:

    ```bash
    # Example: Add new training day
    touch docs/training/day09-extra.md
    ```

2. Edit `mkdocs.yml` to add to navigation:

    ```yaml
    nav:
      - Training Curriculum:
          - training/index.md
          - Day 9 Extra: training/day09-extra.md
    ```

3. Write content using Markdown + extensions

4. Save and verify in browser

## Building for Production

### Build Static Site

```bash
# Clean build
rm -rf site/
mkdocs build

# Build with strict mode (fails on warnings)
mkdocs build --strict

# Build with verbose output
mkdocs build --verbose

# Build to custom directory
mkdocs build --site-dir custom-output/
```

**Output:**

- Static HTML files in `site/` directory
- Fully self-contained
- Ready for any static hosting
- Includes all assets (CSS, JS, images)

### Validate Build

```bash
# Build with strict mode
mkdocs build --strict

# Check for:
# - Broken links
# - Missing files
# - Invalid Markdown
# - Configuration errors
```

**Serve Built Site Locally:**

```bash
# Build site
mkdocs build

# Serve with Python
cd site/
python3 -m http.server 8000

# Access at http://localhost:8000
```

## Deployment Options

### Option 1: GitHub Pages (Recommended)

**Automatic Deployment:**

```bash
# Deploy to gh-pages branch
mkdocs gh-deploy

# With custom commit message
mkdocs gh-deploy -m "Update documentation $(date)"

# Force push (if needed)
mkdocs gh-deploy --force
```

**Setup GitHub Pages:**

1. Go to repository Settings → Pages
2. Source: Deploy from branch
3. Branch: `gh-pages` / `root`
4. Save

Access at: `https://yourusername.github.io/kafka-training-java`

**Custom Domain:**

1. Add `CNAME` file to `docs/`:

    ```bash
    echo "docs.yourdomain.com" > docs/CNAME
    ```

2. Configure DNS:

    ```
    CNAME docs.yourdomain.com -> yourusername.github.io
    ```

3. Enable HTTPS in GitHub Pages settings

### Option 2: Netlify

**Deploy to Netlify:**

1. **Build settings:**

    ```toml
    # netlify.toml
    [build]
      command = "mkdocs build"
      publish = "site"

    [build.environment]
      PYTHON_VERSION = "3.9"
    ```

2. **Connect repository:**
    - Go to Netlify dashboard
    - New site from Git
    - Select repository
    - Deploy

3. **Custom domain:**
    - Domain settings → Add custom domain
    - Update DNS records

### Option 3: Vercel

**Deploy to Vercel:**

1. Install Vercel CLI:

    ```bash
    npm install -g vercel
    ```

2. Deploy:

    ```bash
    # First time
    vercel

    # Production
    vercel --prod
    ```

3. **vercel.json:**

    ```json
    {
      "buildCommand": "mkdocs build",
      "outputDirectory": "site",
      "framework": null
    }
    ```

### Option 4: AWS S3 + CloudFront

**Deploy to AWS:**

```bash
# 1. Build site
mkdocs build

# 2. Sync to S3
aws s3 sync site/ s3://your-bucket-name/ \
  --delete \
  --acl public-read

# 3. Invalidate CloudFront cache
aws cloudfront create-invalidation \
  --distribution-id YOUR_DIST_ID \
  --paths "/*"
```

### Option 5: Docker Container

**Run Documentation in Docker:**

1. **Create Dockerfile.docs:**

    ```dockerfile
    FROM python:3.11-slim

    WORKDIR /docs

    # Install dependencies
    COPY requirements.txt .
    RUN pip install --no-cache-dir -r requirements.txt

    # Copy documentation
    COPY mkdocs.yml .
    COPY docs/ ./docs/

    EXPOSE 8000

    # Serve documentation
    CMD ["mkdocs", "serve", "-a", "0.0.0.0:8000"]
    ```

2. **Build and run:**

    ```bash
    # Build image
    docker build -t kafka-training-docs -f Dockerfile.docs .

    # Run container
    docker run -p 8000:8000 kafka-training-docs

    # Access at http://localhost:8000
    ```

3. **Production build:**

    ```dockerfile
    # Multi-stage build
    FROM python:3.11-slim AS builder
    WORKDIR /docs
    COPY requirements.txt .
    RUN pip install --no-cache-dir -r requirements.txt
    COPY mkdocs.yml .
    COPY docs/ ./docs/
    RUN mkdocs build

    FROM nginx:alpine
    COPY --from=builder /docs/site /usr/share/nginx/html
    EXPOSE 80
    ```

## CI/CD Integration

### GitHub Actions

**Automatic deployment on push:**

```yaml
# .github/workflows/docs.yml
name: Deploy Documentation

on:
  push:
    branches: [master, main]
    paths:
      - 'docs/**'
      - 'mkdocs.yml'
      - 'requirements.txt'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Python
        uses: actions/setup-python@v4
        with:
          python-version: 3.9

      - name: Install dependencies
        run: pip install -r requirements.txt

      - name: Build documentation
        run: mkdocs build --strict

      - name: Deploy to GitHub Pages
        run: mkdocs gh-deploy --force
```

### GitLab CI

```yaml
# .gitlab-ci.yml
pages:
  image: python:3.9
  stage: deploy
  script:
    - pip install -r requirements.txt
    - mkdocs build --strict
  artifacts:
    paths:
      - public
  only:
    - master
```

## Customization

### Theme Configuration

Edit `mkdocs.yml`:

```yaml
theme:
  name: material
  palette:
    - scheme: default
      primary: deep orange      # Kafka orange theme
      accent: orange
  features:
    - navigation.instant       # Instant loading
    - navigation.tracking      # Track scroll position
    - navigation.tabs         # Top-level tabs
    - content.code.copy       # Copy code button
```

### Custom CSS

Edit `docs/stylesheets/extra.css`:

```css
:root {
  --kafka-orange: #ff6600;
}

.kafka-container {
  border-left: 4px solid var(--kafka-orange);
  padding: 1em;
  background-color: rgba(255, 102, 0, 0.05);
}
```

### Custom JavaScript

Edit `docs/javascripts/mermaid-init.js` for Mermaid configuration.

## Troubleshooting

### Build Errors

**Issue: Module not found**

```bash
# Reinstall dependencies
pip install --force-reinstall -r requirements.txt
```

**Issue: Broken links**

```bash
# Build in strict mode to catch errors
mkdocs build --strict
```

**Issue: Theme not loading**

```bash
# Check Material theme is installed
pip list | grep mkdocs-material

# Reinstall
pip install --force-reinstall mkdocs-material
```

### Deployment Issues

**Issue: GitHub Pages not updating**

```bash
# Force deploy
mkdocs gh-deploy --force

# Check gh-pages branch exists
git branch -a | grep gh-pages
```

**Issue: Assets not loading**

- Check `site_url` in mkdocs.yml
- Verify base path configuration
- Check browser console for errors

### Performance Issues

**Slow build times:**

```bash
# Use --dirty flag for faster rebuilds (dev only)
mkdocs build --dirty

# Limit concurrent builds
mkdocs build --no-directory-urls
```

## Maintenance

### Update Dependencies

```bash
# Update all packages
pip install --upgrade -r requirements.txt

# Update specific package
pip install --upgrade mkdocs-material

# Check for outdated packages
pip list --outdated
```

### Backup Documentation

```bash
# Backup docs directory
tar -czf docs-backup-$(date +%Y%m%d).tar.gz docs/

# Backup with Git
git add docs/
git commit -m "Update documentation"
git push
```

### Monitor Site

- Check Google Analytics (if configured)
- Monitor broken links
- Review user feedback
- Update content regularly

## Best Practices

1. **Write in Markdown** - Simple, portable format
2. **Use diagrams** - Mermaid for architecture
3. **Code examples** - Always include working code
4. **Version control** - Commit docs with code
5. **Test locally** - Verify before deploying
6. **Keep updated** - Regular content reviews
7. **Responsive** - Test on mobile devices
8. **Accessible** - Follow WCAG guidelines

## Resources

- [MkDocs Documentation](https://www.mkdocs.org/)
- [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)
- [Markdown Guide](https://www.markdownguide.org/)
- [Mermaid Diagrams](https://mermaid-js.github.io/)

## Support

For issues:

1. Check this guide
2. Review [MkDocs Material docs](https://squidfunk.github.io/mkdocs-material/)
3. Open issue in repository
4. Ask in community forums

---

**Documentation is code!** Keep it updated, tested, and version controlled.
