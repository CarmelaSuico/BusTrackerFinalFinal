const https = require('https');
const urls = [
  'https://gemini.googleapis.com/v1/models/gemini-1.5/assist?key=INVALID',
  'https://gemini.googleapis.com/v1/models/gemini-1.5:generate?key=INVALID',
  'https://generativelanguage.googleapis.com/v1/models/gemini-1.5:generate?key=INVALID',
  'https://generativelanguage.googleapis.com/v1beta2/models/gemini-1.5:generate?key=INVALID',
  'https://generativelanguage.googleapis.com/v1/models?key=INVALID',
  'https://generativelanguage.googleapis.com/v1beta2/models/text-bison-001:generate?key=INVALID',
  'https://generativelanguage.googleapis.com/v1beta2/models/chat-bison-001:generate?key=INVALID'
];

function doUrl(url) {
  return new Promise((resolve) => {
    const req = https.request(url, { method: 'POST', headers: { 'Content-Type': 'application/json' } }, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => resolve({ url, status: res.statusCode, headers: res.headers, body: data }));
    });
    req.on('error', (e) => resolve({ url, error: e.message }));
    req.write(JSON.stringify({ prompt: { text: 'Hello' }, maxOutputTokens: 10 }));
    req.end();
  });
}

(async () => {
  for (const url of urls) {
    const result = await doUrl(url);
    console.log('URL', result.url);
    if (result.error) {
      console.log('ERROR', result.error);
    } else {
      console.log('STATUS', result.status);
      console.log('HEADERS', result.headers['content-type']);
      console.log('BODY', result.body.slice(0, 300));
    }
    console.log('---');
  }
})();
