require('dotenv').config();
const express = require('express');
const cors = require('cors');
const Stripe = require('stripe');

// ---------------------------------------------------------------------------
// Config
// ---------------------------------------------------------------------------

const stripe = Stripe(process.env.STRIPE_SECRET_KEY);
const PORT = process.env.PORT || 3001;
const ALLOWED_ORIGIN = process.env.ALLOWED_ORIGIN || '*';

const path = require('path');
const app = express();
app.use(express.json());
app.use(cors({
  origin: (origin, cb) => cb(null, true),
  methods: ['GET', 'POST'],
}));

// Serve the store page directly from the server
app.use(express.static(path.join(__dirname, '..', 'docs')));

// ---------------------------------------------------------------------------
// Tier definitions  (amount in cents)
// ---------------------------------------------------------------------------

const TIERS = {
  '500':  { points: 500,  bonus: 0,    priceCents: 50,  label: '500 Pulsar Points' },
  '1500': { points: 1500, bonus: 200,  priceCents: 50,  label: '1500 Pulsar Points' },
  '3500': { points: 3500, bonus: 500,  priceCents: 50,  label: '3500 Pulsar Points' },
  '8000': { points: 8000, bonus: 1500, priceCents: 50,  label: '8000 Pulsar Points' },
};

// ---------------------------------------------------------------------------
// POST /create-payment-intent
//
// Body: { tier: "500" | "1500" | "3500" | "8000" }
// Returns: { clientSecret, publishableKey, tier }
// ---------------------------------------------------------------------------

app.post('/create-payment-intent', async (req, res) => {
  try {
    const { tier: tierKey } = req.body;
    const tier = TIERS[tierKey];

    if (!tier) {
      return res.status(400).json({ error: 'Invalid tier. Must be one of: 500, 1500, 3500, 8000' });
    }

    const paymentIntent = await stripe.paymentIntents.create({
      amount: tier.priceCents,
      currency: 'usd',
      description: tier.label,
      metadata: {
        tier: tierKey,
        points: String(tier.points),
        bonus: String(tier.bonus),
        totalPoints: String(tier.points + tier.bonus),
      },
      automatic_payment_methods: { enabled: true },
      // These are enabled automatically by Stripe based on your Dashboard settings.
      // To show Apple Pay/Google Pay, the page MUST be served over HTTPS.
      // Enable them at: https://dashboard.stripe.com/settings/payment_methods
    });

    res.json({
      clientSecret: paymentIntent.client_secret,
      publishableKey: process.env.STRIPE_PUBLISHABLE_KEY,
      tier: {
        points: tier.points,
        bonus: tier.bonus,
        total: tier.points + tier.bonus,
      },
    });
  } catch (err) {
    console.error('Error creating PaymentIntent:', err.message);
    res.status(500).json({ error: err.message });
  }
});

// ---------------------------------------------------------------------------
// POST /verify-payment
//
// Body: { paymentIntentId: "pi_xxx" }
// Returns: { success, points, bonus, totalPoints }
// ---------------------------------------------------------------------------

app.post('/verify-payment', async (req, res) => {
  try {
    const { paymentIntentId } = req.body;

    if (!paymentIntentId) {
      return res.status(400).json({ error: 'Missing paymentIntentId' });
    }

    const paymentIntent = await stripe.paymentIntents.retrieve(paymentIntentId);

    if (paymentIntent.status === 'succeeded') {
      const { points, bonus, totalPoints } = paymentIntent.metadata;
      res.json({
        success: true,
        points: parseInt(points),
        bonus: parseInt(bonus),
        totalPoints: parseInt(totalPoints),
      });
    } else {
      res.json({
        success: false,
        status: paymentIntent.status,
        error: `Payment status: ${paymentIntent.status}`,
      });
    }
  } catch (err) {
    console.error('Error verifying payment:', err.message);
    res.status(500).json({ error: err.message });
  }
});

// ---------------------------------------------------------------------------
// Health check
// ---------------------------------------------------------------------------

app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// ---------------------------------------------------------------------------
// Start (HTTPS for Apple Pay/Google Pay, HTTP fallback)
// ---------------------------------------------------------------------------

const fs = require('fs');
const https = require('https');
const HTTPS_PORT = parseInt(PORT) + 1; // 3002

try {
  const sslOptions = {
    key: fs.readFileSync(path.join(__dirname, 'key.pem')),
    cert: fs.readFileSync(path.join(__dirname, 'cert.pem')),
  };
  https.createServer(sslOptions, app).listen(HTTPS_PORT, () => {
    console.log(`Pulsar Store (HTTPS) running on https://localhost:${HTTPS_PORT}`);
    console.log(`  → Apple Pay, Google Pay, Link available over HTTPS`);
  });
} catch (e) {
  console.log('HTTPS not available (no cert.pem/key.pem). Apple Pay/Google Pay won\'t work locally.');
}

app.listen(PORT, () => {
  console.log(`Pulsar Store (HTTP) running on http://localhost:${PORT}`);
  console.log(`CORS origin: ${ALLOWED_ORIGIN}`);
  if (!process.env.STRIPE_SECRET_KEY || process.env.STRIPE_SECRET_KEY.startsWith('sk_test_XXX')) {
    console.warn('\n⚠  WARNING: STRIPE_SECRET_KEY is not set or still a placeholder.');
    console.warn('   Get your keys from https://dashboard.stripe.com/apikeys\n');
  }
});
