export default async function handler(req, res) {
  const platform = req.query.platform || 'mac';

  try {
    // Fetch latest release from GitHub API
    const ghRes = await fetch('https://api.github.com/repos/ericpnag/pulsar/releases/latest', {
      headers: { 'User-Agent': 'PulsarClient/1.0' }
    });
    const release = await ghRes.json();

    // Find the right asset
    let asset = null;
    for (const a of release.assets || []) {
      const name = a.name.toLowerCase();
      if (platform === 'mac' && name.endsWith('.dmg')) asset = a;
      if (platform === 'windows' && (name.endsWith('.exe') || name.endsWith('.msi'))) asset = a;
      if (platform === 'linux' && (name.endsWith('.appimage') || name.endsWith('.deb'))) asset = a;
    }

    if (asset) {
      // Redirect directly to the download URL
      res.writeHead(302, { Location: asset.browser_download_url });
      res.end();
    } else {
      // Fallback to releases page
      res.writeHead(302, { Location: release.html_url || 'https://github.com/ericpnag/pulsar/releases/latest' });
      res.end();
    }
  } catch {
    res.writeHead(302, { Location: 'https://github.com/ericpnag/pulsar/releases/latest' });
    res.end();
  }
}
