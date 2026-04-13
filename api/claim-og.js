// Track OG cape claims using Vercel KV-like approach (simple JSON in Stripe metadata)
// We use a Stripe product to store the claim count since we don't have a database

const MAX_CLAIMS = 100;

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  if (req.method === 'OPTIONS') return res.status(200).end();

  const Stripe = (await import('stripe')).default;
  const stripe = new Stripe(process.env.STRIPE_SECRET_KEY);

  // Use a Stripe product to store claim count (hacky but works without a DB)
  const PRODUCT_ID = 'og_cape_tracker';

  async function getClaimCount() {
    try {
      const products = await stripe.products.search({ query: `metadata['type']:'og_claim_counter'` });
      if (products.data.length > 0) {
        return parseInt(products.data[0].metadata.count || '0');
      }
      // Create tracker product
      await stripe.products.create({
        id: PRODUCT_ID,
        name: 'OG Cape Claim Tracker (DO NOT DELETE)',
        metadata: { type: 'og_claim_counter', count: '0' },
      });
      return 0;
    } catch {
      return 0;
    }
  }

  async function incrementClaims() {
    const count = await getClaimCount();
    if (count >= MAX_CLAIMS) return { success: false, count, remaining: 0 };
    const newCount = count + 1;
    try {
      await stripe.products.update(PRODUCT_ID, { metadata: { type: 'og_claim_counter', count: String(newCount) } });
    } catch {
      // Product might not exist with that ID, try search
      const products = await stripe.products.search({ query: `metadata['type']:'og_claim_counter'` });
      if (products.data.length > 0) {
        await stripe.products.update(products.data[0].id, { metadata: { type: 'og_claim_counter', count: String(newCount) } });
      }
    }
    return { success: true, count: newCount, remaining: MAX_CLAIMS - newCount };
  }

  if (req.method === 'GET') {
    const count = await getClaimCount();
    return res.json({ count, remaining: MAX_CLAIMS - count, max: MAX_CLAIMS });
  }

  if (req.method === 'POST') {
    const result = await incrementClaims();
    return res.json(result);
  }

  res.status(405).json({ error: 'Method not allowed' });
}
