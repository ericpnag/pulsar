const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY);

module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  if (req.method === 'OPTIONS') return res.status(200).end();

  try {
    const { paymentIntentId } = req.body;
    if (!paymentIntentId) return res.status(400).json({ error: 'Missing paymentIntentId' });

    const paymentIntent = await stripe.paymentIntents.retrieve(paymentIntentId);

    if (paymentIntent.status === 'succeeded') {
      const { points, bonus, totalPoints } = paymentIntent.metadata;
      res.json({ success: true, points: parseInt(points), bonus: parseInt(bonus), totalPoints: parseInt(totalPoints) });
    } else {
      res.json({ success: false, status: paymentIntent.status, error: `Payment status: ${paymentIntent.status}` });
    }
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};
