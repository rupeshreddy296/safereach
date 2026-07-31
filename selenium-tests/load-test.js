const http = require('http');
const url = require('url');

const targetUrl = 'http://localhost/safereach/api/hospitals/list.php'; 
const concurrency = 100;
const durationMs = 60000; // 1 minute

let totalRequests = 0;
let successfulRequests = 0;
let failedRequests = 0;
const latencies = [];

let running = true;

function sendRequest() {
    if (!running) return;

    const start = Date.now();
    const parsedUrl = url.parse(targetUrl);
    
    const req = http.get({
        hostname: parsedUrl.hostname,
        port: parsedUrl.port || 80,
        path: parsedUrl.path,
        headers: {
            'Accept': 'application/json'
        }
    }, (res) => {
        const latency = Date.now() - start;
        latencies.push(latency);
        totalRequests++;
        if (res.statusCode === 200) {
            successfulRequests++;
        } else {
            failedRequests++;
        }
        res.resume();
        sendRequest();
    });

    req.on('error', (e) => {
        totalRequests++;
        failedRequests++;
        sendRequest();
    });
}

console.log(`Starting load test against: ${targetUrl}`);
console.log(`Config: ${concurrency} concurrent virtual users, duration: 1 minute...\n`);

const startTime = Date.now();

for (let i = 0; i < concurrency; i++) {
    sendRequest();
}

setTimeout(() => {
    running = false;
    const actualDuration = (Date.now() - startTime) / 1000;
    
    console.log("========================================");
    console.log("           LOAD TEST RESULTS            ");
    console.log("========================================");
    
    const rps = (totalRequests / actualDuration).toFixed(2);
    console.log(`Duration: ${actualDuration.toFixed(2)} seconds`);
    console.log(`Total Requests: ${totalRequests}`);
    console.log(`Successful Requests: ${successfulRequests}`);
    console.log(`Failed Requests: ${failedRequests}`);
    console.log(`Requests Per Second (RPS): ${rps} req/sec`);
    
    if (latencies.length > 0) {
        latencies.sort((a, b) => a - b);
        const sum = latencies.reduce((a, b) => a + b, 0);
        const avg = (sum / latencies.length).toFixed(2);
        const min = latencies[0];
        const max = latencies[latencies.length - 1];
        
        console.log("\nResponse Time (Latency):");
        console.log(`  Average: ${avg}ms`);
        console.log(`  Min:     ${min}ms`);
        console.log(`  Max:     ${max}ms`);
    } else {
        console.log("\nNo latency samples recorded.");
    }
    console.log("========================================");
    
    process.exit(0);
}, durationMs);
