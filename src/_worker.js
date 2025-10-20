/**
 * Cloudflare Worker for serving MkDocs static site
 * Deployed at subdomain: kafka-training.stratpoint.io
 */

import { getAssetFromKV, mapRequestToAsset } from '@cloudflare/kv-asset-handler';
import manifestJSON from '__STATIC_CONTENT_MANIFEST';
const assetManifest = JSON.parse(manifestJSON);

export default {
  async fetch(request, env, ctx) {
    try {
      const url = new URL(request.url);

      // Get the asset using default behavior with mapRequestToAsset
      let response = await getAssetFromKV(
        {
          request,
          waitUntil: ctx.waitUntil.bind(ctx)
        },
        {
          ASSET_NAMESPACE: env.__STATIC_CONTENT,
          ASSET_MANIFEST: assetManifest,
          mapRequestToAsset: (req) => {
            const url = new URL(req.url);
            let pathname = url.pathname;

            // Serve index.html for directory paths
            if (pathname === '/' || pathname.endsWith('/')) {
              pathname = pathname + 'index.html';
            }
            // Try .html extension for paths without extensions
            else if (!pathname.includes('.')) {
              pathname = pathname + '/index.html';
            }

            url.pathname = pathname;
            return new Request(url.toString(), req);
          }
        }
      );

      // Clone the response to modify headers
      response = new Response(response.body, response);

      // Add caching headers
      response.headers.set('Cache-Control', 'public, max-age=3600');

      // Add security headers
      response.headers.set('X-Content-Type-Options', 'nosniff');
      response.headers.set('X-XSS-Protection', '1; mode=block');

      // Allow iframe embedding - remove X-Frame-Options and use permissive CSP
      // This allows the docs to be embedded in Moodle or other platforms
      response.headers.delete('X-Frame-Options');
      response.headers.set('Content-Security-Policy', "frame-ancestors 'self' *");

      return response;
    } catch (e) {
      // Return 404
      return new Response('Not Found: ' + e.message, { status: 404 });
    }
  },
};
