const http = require('http');
const fs = require('fs');
const path = require('path');
const https = require('https');

const PORT = process.env.PORT || 3000;
const APK_PATH = path.join(__dirname, 'CSS_Compass.apk');
const STUDENTS_FILE = path.join(__dirname, 'students.json');
const CONFIG_FILE = path.join(__dirname, 'config.json');

// Try to load Gemini API key from environment variable or .dev.env.json
let geminiApiKey = process.env.GEMINI_API_KEY || "";
if (!geminiApiKey) {
  try {
    if (fs.existsSync(path.join(__dirname, '.dev.env.json'))) {
      const envJson = JSON.parse(fs.readFileSync(path.join(__dirname, '.dev.env.json'), 'utf8'));
      geminiApiKey = envJson.GEMINI_API_KEY || "";
    }
  } catch (e) {
    console.log("Note: Could not load .dev.env.json: " + e.message);
  }
}

// Ensure database files exist with initial seeds
if (!fs.existsSync(STUDENTS_FILE)) {
  fs.writeFileSync(STUDENTS_FILE, JSON.stringify([
    {
      id: "std_1",
      name: "Syed Muhammad Ali",
      username: "ali_css2026",
      email: "ali.css@gmail.com",
      password: "Pakistan123!",
      isPaid: true,
      phone: "+923001234567",
      createdAt: new Date().toISOString()
    },
    {
      id: "std_2",
      name: "Fatima Bhutto",
      username: "css_aspirant2026",
      email: "student.css@gmail.com",
      password: "Pakistan123!",
      isPaid: false,
      phone: "+923007654321",
      createdAt: new Date().toISOString()
    }
  ], null, 2));
}

const defaultConfig = {
  adminUsername: "admin",
  adminPassword: "csscompass2026",
  whatsappNumber: "+923001234567",
  firebaseConfig: {
    apiKey: "",
    databaseUrl: "",
    projectId: ""
  }
};

if (!fs.existsSync(CONFIG_FILE)) {
  fs.writeFileSync(CONFIG_FILE, JSON.stringify(defaultConfig, null, 2));
}

// Helpers for loading/saving
function loadStudents() {
  try {
    const list = JSON.parse(fs.readFileSync(STUDENTS_FILE, 'utf8'));
    return list.map(s => ({
      ...s,
      username: s.username || s.email.split('@')[0]
    }));
  } catch (e) {
    return [];
  }
}

function saveStudents(students) {
  fs.writeFileSync(STUDENTS_FILE, JSON.stringify(students, null, 2));
}

function loadConfig() {
  try {
    return JSON.parse(fs.readFileSync(CONFIG_FILE, 'utf8'));
  } catch (e) {
    return defaultConfig;
  }
}

function saveConfig(config) {
  fs.writeFileSync(CONFIG_FILE, JSON.stringify(config, null, 2));
}

// Firebase Sync logic
function syncToFirebase(students, config, callback) {
  if (!config.firebaseConfig || !config.firebaseConfig.databaseUrl) {
    return callback(new Error("Firebase is not configured"), null);
  }
  let dbUrl = config.firebaseConfig.databaseUrl.replace(/\/$/, "");
  let url = `${dbUrl}/students.json`;
  if (config.firebaseConfig.apiKey) {
    url += `?auth=${config.firebaseConfig.apiKey}`;
  }

  const parsedUrl = new URL(url);
  const dataString = JSON.stringify(students);
  const options = {
    hostname: parsedUrl.hostname,
    port: parsedUrl.port || 443,
    path: parsedUrl.pathname + parsedUrl.search,
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(dataString)
    }
  };

  const req = https.request(options, (res) => {
    let body = '';
    res.on('data', (chunk) => body += chunk);
    res.on('end', () => {
      if (res.statusCode >= 200 && res.statusCode < 300) {
        callback(null, JSON.parse(body));
      } else {
        callback(new Error(`Firebase Error (${res.statusCode}): ${body}`), null);
      }
    });
  });

  req.on('error', (e) => {
    callback(e, null);
  });

  req.write(dataString);
  req.end();
}

function pullFromFirebase(config, callback) {
  if (!config.firebaseConfig || !config.firebaseConfig.databaseUrl) {
    return callback(new Error("Firebase is not configured"), null);
  }
  let dbUrl = config.firebaseConfig.databaseUrl.replace(/\/$/, "");
  let url = `${dbUrl}/students.json`;
  if (config.firebaseConfig.apiKey) {
    url += `?auth=${config.firebaseConfig.apiKey}`;
  }

  const parsedUrl = new URL(url);
  const options = {
    hostname: parsedUrl.hostname,
    port: parsedUrl.port || 443,
    path: parsedUrl.pathname + parsedUrl.search,
    method: 'GET',
    headers: {
      'Content-Type': 'application/json'
    }
  };

  const req = https.request(options, (res) => {
    let body = '';
    res.on('data', (chunk) => body += chunk);
    res.on('end', () => {
      if (res.statusCode >= 200 && res.statusCode < 300) {
        try {
          if (!body || body.trim() === 'null') {
            return callback(null, []);
          }
          const data = JSON.parse(body);
          let studentsList = [];
          if (Array.isArray(data)) {
            studentsList = data.filter(s => s !== null);
          } else if (data && typeof data === 'object') {
            studentsList = Object.keys(data).map(key => {
              const item = data[key];
              if (item && typeof item === 'object') {
                return { id: key, ...item };
              }
              return item;
            });
          }
          callback(null, studentsList);
        } catch (e) {
          callback(e, null);
        }
      } else {
        callback(new Error(`Firebase Error (${res.statusCode}): ${body}`), null);
      }
    });
  });

  req.on('error', (e) => {
    callback(e, null);
  });

  req.end();
}

// Gemini integration logic
function callGemini(prompt, systemInstruction, callback) {
  if (!geminiApiKey) {
    return callback(new Error("Gemini API key is not configured in AI Studio secrets"), null);
  }
  const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${geminiApiKey}`;
  const parsedUrl = new URL(url);
  
  const payload = {
    contents: [{ parts: [{ text: prompt }] }]
  };
  if (systemInstruction) {
    payload.systemInstruction = { parts: [{ text: systemInstruction }] };
  }
  
  const dataString = JSON.stringify(payload);
  const options = {
    hostname: parsedUrl.hostname,
    port: 443,
    path: parsedUrl.pathname + parsedUrl.search,
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(dataString)
    }
  };

  const req = https.request(options, (res) => {
    let body = '';
    res.on('data', chunk => body += chunk);
    res.on('end', () => {
      if (res.statusCode >= 200 && res.statusCode < 300) {
        try {
          const json = JSON.parse(body);
          const reply = json.candidates?.[0]?.content?.parts?.[0]?.text || "I was unable to analyze your query. Let's try again.";
          callback(null, reply);
        } catch (e) {
          callback(e, null);
        }
      } else {
        callback(new Error(`Gemini API returned status ${res.statusCode}: ${body}`), null);
      }
    });
  });

  req.on('error', (e) => {
    callback(e, null);
  });

  req.write(dataString);
  req.end();
}

// Helper to parse JSON request body
function parseJsonBody(req, callback) {
  let body = '';
  req.on('data', chunk => body += chunk);
  req.on('end', () => {
    try {
      callback(null, JSON.parse(body));
    } catch (e) {
      callback(e, null);
    }
  });
}

// HTTP Server Router
const server = http.createServer((req, res) => {
  const url = req.url;
  const parsedUrlForRoute = new URL(url, 'http://localhost');
  const pathname = parsedUrlForRoute.pathname;
  const method = req.method;

  // Add CORS headers for browser compatibility across dev and shared environments
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS, HEAD');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, X-Requested-With');

  // Handle CORS Preflight
  if (method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // Serve Android APK
  const isDownloadQuery = parsedUrlForRoute.searchParams.get('download') === '1' || parsedUrlForRoute.searchParams.get('download') === 'true';

  // Handle HEAD Requests (e.g. proxy/health checks)
  if (method === 'HEAD') {
    if (isDownloadQuery || pathname === '/download' || pathname === '/CSS_Compass.apk') {
      res.writeHead(200, { 'Content-Type': 'application/vnd.android.package-archive' });
    } else {
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
    }
    res.end();
    return;
  }
  if (isDownloadQuery || pathname === '/download' || pathname === '/download/' || pathname === '/CSS_Compass.apk') {
    const candidatePaths = [
      path.join(__dirname, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk'),
      path.join(__dirname, '.build-outputs', 'app-debug.apk'),
      APK_PATH
    ];
    let validApkPath = null;
    let maxSizeBytes = 0;
    for (const p of candidatePaths) {
      if (fs.existsSync(p)) {
        const s = fs.statSync(p).size;
        if (s > maxSizeBytes) {
          maxSizeBytes = s;
          validApkPath = p;
        }
      }
    }

    if (validApkPath) {
      const stat = fs.statSync(validApkPath);
      res.writeHead(200, {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Length': stat.size,
        'Content-Disposition': 'attachment; filename="CSS_Compass.apk"',
        'Cache-Control': 'no-cache, no-store, must-revalidate',
        'Pragma': 'no-cache',
        'Expires': '0'
      });
      const readStream = fs.createReadStream(validApkPath);
      readStream.on('error', (err) => {
        console.error("APK readStream error:", err);
        if (!res.headersSent) {
          res.writeHead(500, { 'Content-Type': 'text/plain' });
          res.end("Server Error reading APK");
        }
      });
      readStream.pipe(res);
    } else {
      res.writeHead(404, { 'Content-Type': 'text/html; charset=utf-8' });
      res.end(`
        <html>
          <body style="font-family: sans-serif; background-color: #0b0f19; color: #f8fafc; display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh; margin: 0;">
            <h1 style="color: #f59e0b;">APK File Not Found</h1>
            <p>Please compile the applet first so that the APK binary is created.</p>
          </body>
        </html>
      `);
    }
    return;
  }

  // Serve Main Webpage (Portal) - Landing, Admin, Student, or SPA routes
  if (method === 'GET' && !pathname.startsWith('/api/')) {
    res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
    res.end(getHtmlContent());
    return;
  }

  // --- API ROUTING ---

  // Student/Admin Login Endpoint
  if (pathname === '/api/login' && method === 'POST') {
    parseJsonBody(req, (err, body) => {
      console.log(`[LOGIN ATTEMPT] Received login request. Error: ${!!err}, Body: ${JSON.stringify(body)}`);
      if (err || !body || !body.email || !body.password) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, message: "Missing email or password" }));
        return;
      }

      const email = body.email.toLowerCase().trim();
      const password = body.password.trim();

      // Check for Admin Account
      const config = loadConfig();
      if (email === config.adminUsername.toLowerCase() && password === config.adminPassword) {
        console.log(`[LOGIN SUCCESS] Admin logged in`);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, isAdmin: true, message: "Admin authenticated successfully!" }));
        return;
      }

      // Check Student Accounts (Local First)
      const students = loadStudents();
      const student = students.find(s => 
        (s.email.toLowerCase() === email || (s.username && s.username.toLowerCase() === email)) && 
        s.password === password
      );

      if (student) {
        // Enforce Device Hardware Binding Process
        const incomingDeviceId = body.deviceId ? body.deviceId.trim() : null;
        if (incomingDeviceId) {
          if (!student.deviceId) {
            // Bind device on first login
            student.deviceId = incomingDeviceId;
            saveStudents(students);
            
            // Sync to Firebase if configured
            if (config.firebaseConfig && config.firebaseConfig.databaseUrl) {
              syncToFirebase(students, config, (fbErr) => {
                if (fbErr) console.error("Firebase device bind sync failed:", fbErr.message);
              });
            }
            console.log(`[DEVICE BOUND] Bound device ${incomingDeviceId} to student ${student.email}`);
          } else if (student.deviceId !== incomingDeviceId) {
            // Device mismatch! Block access
            console.log(`[LOGIN BLOCKED] Device mismatch. Bound: ${student.deviceId}, Incoming: ${incomingDeviceId}`);
            res.writeHead(403, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({
              success: false,
              message: "Device mismatch: This account is bound to another device. Please contact Admin to reset."
            }));
            return;
          }
        }

        console.log(`[LOGIN SUCCESS] Student logged in: ${student.email}`);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          success: true,
          isAdmin: false,
          student: {
            id: student.id,
            name: student.name,
            username: student.username || student.email.split('@')[0],
            email: student.email,
            isPaid: student.isPaid,
            phone: student.phone,
            deviceId: student.deviceId || null
          }
        }));
      } else {
        console.log(`[LOGIN FAILED] Invalid credentials for email: ${email}`);
        res.writeHead(401, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, message: "Invalid email or password. Please verify or ask Admin for access." }));
      }
    });
    return;
  }

  // Students Management API (Admin only)
  if (pathname === '/api/students' && method === 'GET') {
    const students = loadStudents();
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(students));
    return;
  }

  if (pathname === '/api/students' && method === 'POST') {
    parseJsonBody(req, (err, newStudent) => {
      if (err || !newStudent || !newStudent.name || !newStudent.email || !newStudent.password) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, message: "Invalid student payload" }));
        return;
      }

      const students = loadStudents();
      const config = loadConfig();
      
      const index = students.findIndex(s => s.email.toLowerCase() === newStudent.email.toLowerCase());
      const usernameVal = newStudent.username ? newStudent.username.trim() : (newStudent.email.split('@')[0]);
      if (index >= 0) {
        // Update existing student
        students[index] = {
          ...students[index],
          name: newStudent.name,
          username: usernameVal,
          password: newStudent.password,
          isPaid: !!newStudent.isPaid,
          phone: newStudent.phone || ""
        };
      } else {
        // Create new student
        students.push({
          id: "std_" + Math.random().toString(36).substr(2, 9),
          name: newStudent.name,
          username: usernameVal,
          email: newStudent.email.toLowerCase().trim(),
          password: newStudent.password,
          isPaid: !!newStudent.isPaid,
          phone: newStudent.phone || "",
          createdAt: new Date().toISOString()
        });
      }

      saveStudents(students);

      // Async Sync to Firebase if configured
      if (config.firebaseConfig && config.firebaseConfig.databaseUrl) {
        syncToFirebase(students, config, (fbErr) => {
          if (fbErr) console.error("Firebase background sync failed:", fbErr.message);
          else console.log("Firebase sync completed successfully after student modify");
        });
      }

      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: true, students }));
    });
    return;
  }

  if (pathname === '/api/students' && method === 'DELETE') {
    const parsedUrl = new URL(url, 'http://localhost');
    const id = parsedUrl.searchParams.get('id');

    if (!id) {
      res.writeHead(400, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: false, message: "Missing student ID" }));
      return;
    }

    let students = loadStudents();
    const config = loadConfig();
    students = students.filter(s => s.id !== id);
    saveStudents(students);

    // Sync deletion to Firebase
    if (config.firebaseConfig && config.firebaseConfig.databaseUrl) {
      syncToFirebase(students, config, (fbErr) => {
        if (fbErr) console.error("Firebase deletion sync failed:", fbErr.message);
      });
    }

    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ success: true, students }));
    return;
  }

  // Reset Student Bound Device API
  if (pathname === '/api/students/reset-device' && method === 'POST') {
    const parsedUrl = new URL(url, 'http://localhost');
    const id = parsedUrl.searchParams.get('id');

    if (!id) {
      res.writeHead(400, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: false, message: "Missing student ID" }));
      return;
    }

    const students = loadStudents();
    const config = loadConfig();
    const student = students.find(s => s.id === id);

    if (student) {
      student.deviceId = null;
      saveStudents(students);

      // Sync reset to Firebase
      if (config.firebaseConfig && config.firebaseConfig.databaseUrl) {
        syncToFirebase(students, config, (fbErr) => {
          if (fbErr) console.error("Firebase device reset sync failed:", fbErr.message);
        });
      }

      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: true, students }));
    } else {
      res.writeHead(404, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: false, message: "Student account not found." }));
    }
    return;
  }

  // Change Password & Profile API (Student)
  if (pathname === '/api/change-password' && method === 'POST') {
    parseJsonBody(req, (err, body) => {
      if (err || !body || !body.email || !body.oldPassword) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, message: "Missing current credentials or details." }));
        return;
      }

      const email = body.email.toLowerCase().trim();
      const oldPassword = body.oldPassword.trim();
      const newPassword = body.newPassword ? body.newPassword.trim() : null;
      const newUsername = body.newUsername ? body.newUsername.trim() : null;
      const newEmail = body.newEmail ? body.newEmail.toLowerCase().trim() : null;

      const students = loadStudents();
      const config = loadConfig();
      const student = students.find(s => s.email.toLowerCase() === email || (s.username && s.username.toLowerCase() === email));

      if (!student) {
        res.writeHead(404, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, message: "Student account not found." }));
        return;
      }

      if (student.password !== oldPassword) {
        res.writeHead(401, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, message: "Incorrect current password." }));
        return;
      }

      if (newPassword && newPassword.length >= 4) {
        student.password = newPassword;
      }
      if (newUsername && newUsername.length >= 3) {
        student.username = newUsername;
      }
      if (newEmail && newEmail.includes('@')) {
        student.email = newEmail;
      }

      saveStudents(students);

      // Sync updated credentials to Firebase
      if (config.firebaseConfig && config.firebaseConfig.databaseUrl) {
        syncToFirebase(students, config, (fbErr) => {
          if (fbErr) console.error("Firebase profile update sync failed:", fbErr.message);
        });
      }

      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        success: true,
        message: "Credentials updated successfully.",
        student: {
          id: student.id,
          name: student.name,
          username: student.username || student.email.split('@')[0],
          email: student.email,
          isPaid: student.isPaid,
          phone: student.phone,
          deviceId: student.deviceId || null
        }
      }));
    });
    return;
  }

  // Config Management API
  if (pathname === '/api/config' && method === 'GET') {
    const config = loadConfig();
    // Exclude the sensitive admin password
    const safeConfig = {
      whatsappNumber: config.whatsappNumber,
      adminUsername: config.adminUsername || "admin",
      firebaseConfig: {
        databaseUrl: config.firebaseConfig ? config.firebaseConfig.databaseUrl || "" : "",
        projectId: config.firebaseConfig ? config.firebaseConfig.projectId || "" : "",
        hasApiKey: !!(config.firebaseConfig && config.firebaseConfig.apiKey)
      }
    };
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(safeConfig));
    return;
  }

  if (pathname === '/api/config' && method === 'POST') {
    parseJsonBody(req, (err, body) => {
      if (err || !body) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, message: "Invalid configuration payload" }));
        return;
      }

      const config = loadConfig();
      if (body.whatsappNumber) config.whatsappNumber = body.whatsappNumber.trim();
      if (body.adminUsername) config.adminUsername = body.adminUsername.trim();
      if (body.adminPassword) config.adminPassword = body.adminPassword.trim();
      
      saveConfig(config);
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: true, message: "Configuration saved successfully!", adminUsername: config.adminUsername }));
    });
    return;
  }

  // Firebase Setup and Synchronization Controller (Admin only)
  if (pathname === '/api/firebase' && method === 'POST') {
    parseJsonBody(req, (err, fbSetup) => {
      if (err || !fbSetup) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, message: "Invalid payload" }));
        return;
      }

      const config = loadConfig();
      config.firebaseConfig = {
        apiKey: fbSetup.apiKey || "",
        databaseUrl: fbSetup.databaseUrl || "",
        projectId: fbSetup.projectId || ""
      };
      saveConfig(config);

      // Perform a direct Push/Pull sync
      if (fbSetup.action === 'push') {
        const students = loadStudents();
        syncToFirebase(students, config, (fbErr, data) => {
          if (fbErr) {
            res.writeHead(500, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: "Firebase connection error: " + fbErr.message }));
          } else {
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: true, message: "Pushed students database successfully to Firebase!", data }));
          }
        });
      } else if (fbSetup.action === 'pull') {
        pullFromFirebase(config, (fbErr, firebaseStudents) => {
          if (fbErr) {
            res.writeHead(500, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: false, message: "Firebase load failed: " + fbErr.message }));
          } else {
            if (firebaseStudents && firebaseStudents.length > 0) {
              saveStudents(firebaseStudents);
            }
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: true, message: `Successfully pulled ${firebaseStudents ? firebaseStudents.length : 0} students from Firebase!`, students: firebaseStudents }));
          }
        });
      } else {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, message: "Firebase details saved successfully." }));
      }
    });
    return;
  }

  // Gemini AI Chat Proxy for Web Companion App
  if (pathname === '/api/gemini' && method === 'POST') {
    parseJsonBody(req, (err, body) => {
      if (err || !body || !body.prompt) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, message: "A valid prompt is required for Gemini AI Coach" }));
        return;
      }

      const systemPrompt = `
        You are an elite, highly encouraging AI Tutor for CSS (Central Superior Services) exam preparation in Pakistan.
        You explain complex syllabus questions, give direct outline guides, quote references to essential texts (by Hamid Khan, Ikram Rabbani, etc.), and provide clear, bullet-pointed structure.
      `;

      callGemini(body.prompt, systemPrompt, (geminiErr, reply) => {
        if (geminiErr) {
          res.writeHead(500, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: false, message: geminiErr.message }));
        } else {
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: true, reply }));
        }
      });
    });
    return;
  }

  // Catch-all SPA fallback for non-API GET requests
  if (method === 'GET' && !pathname.startsWith('/api/')) {
    res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
    res.end(getHtmlContent());
    return;
  }

  res.writeHead(404, { 'Content-Type': 'text/plain' });
  res.end('Not found');
});

// Global error handlers to prevent server crashes
process.on('uncaughtException', (err) => {
  console.error('Uncaught Exception caught:', err);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('Unhandled Rejection caught at:', promise, 'reason:', reason);
});

// Handle server startup errors (e.g. port already bound)
server.on('error', (e) => {
  if (e.code === 'EADDRINUSE') {
    console.error(`Port ${PORT} is already in use by another process. Continuing execution.`);
  } else {
    console.error('Server startup error:', e);
  }
});

process.on('uncaughtException', (err) => {
  console.error('[CRITICAL] Uncaught Exception caught to prevent server shutdown:', err);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('[CRITICAL] Unhandled Rejection caught to prevent server shutdown:', reason);
});

// Start listening on primary PORT
server.listen(PORT, '0.0.0.0', () => {
  console.log(`Primary server is running at http://0.0.0.0:${PORT}`);
});

// Secondary port binding so both 8080 and 3000 respond
const SECONDARY_PORT = (PORT === 8080 || PORT === '8080') ? 3000 : 8080;
const secondaryServer = http.createServer((req, res) => server.emit('request', req, res));
secondaryServer.on('error', (err) => {
  console.log(`Note: Secondary port ${SECONDARY_PORT} binding status:`, err.message);
});
secondaryServer.listen(SECONDARY_PORT, '0.0.0.0', () => {
  console.log(`Secondary server listening on http://0.0.0.0:${SECONDARY_PORT}`);
});

// Single-File Interactive HTML/JS Web Portal (Landing, Student App, Admin Control Panel)
function getHtmlContent() {
  return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CSS Compass - Commercial Student Portal</title>
    <!-- Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- FontAwesome Icons -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap');
        body {
            font-family: 'Plus Jakarta Sans', sans-serif;
        }
        .code-font {
            font-family: 'JetBrains Mono', monospace;
        }
        .glass-panel {
            background: rgba(15, 23, 42, 0.7);
            backdrop-filter: blur(12px);
            border: 1px solid rgba(51, 65, 85, 0.25);
        }
        .gradient-text {
            background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .custom-scrollbar::-webkit-scrollbar {
            width: 6px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
            background: transparent;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
            background: rgba(148, 163, 184, 0.2);
            border-radius: 10px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
            background: rgba(148, 163, 184, 0.4);
        }
        .hidden {
            display: none !important;
        }
    </style>
</head>
<body class="bg-slate-950 text-slate-100 min-h-screen flex flex-col justify-between selection:bg-amber-500/30 selection:text-amber-200">

    <!-- Top Ambient Glow -->
    <div class="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-7xl h-[350px] bg-gradient-to-b from-amber-500/5 via-rose-500/5 to-transparent blur-3xl pointer-events-none rounded-full"></div>

    <!-- MAIN HEADER -->
    <header class="relative z-10 border-b border-slate-900 bg-slate-950/80 backdrop-blur-md">
        <div class="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
            <div class="flex items-center space-x-3 cursor-pointer" onclick="navigateTo('landing')">
                <div class="p-2 bg-gradient-to-br from-amber-400 to-rose-600 rounded-xl shadow-lg shadow-orange-500/10">
                    <i class="fa-solid fa-compass text-slate-950 text-xl"></i>
                </div>
                <div>
                    <span class="text-lg font-black tracking-tight uppercase bg-gradient-to-r from-amber-300 via-rose-400 to-amber-200 bg-clip-text text-transparent">CSS COMPASS</span>
                    <span class="hidden sm:inline-block text-[10px] font-bold tracking-widest text-slate-500 uppercase ml-2 border-l border-slate-800 pl-2">Academy Portal</span>
                </div>
            </div>

            <!-- Header Action Items -->
            <div class="flex items-center space-x-2 sm:space-x-3">
                <button type="button" onclick="navigateTo('landing')" id="home-nav-btn" class="text-xs font-bold text-slate-400 hover:text-white transition py-1.5 px-3 rounded-lg hover:bg-slate-900 cursor-pointer">Home</button>
                <button type="button" onclick="navigateTo('student')" id="student-nav-btn" class="text-xs font-bold text-slate-400 hover:text-white transition py-1.5 px-3 rounded-lg hover:bg-slate-900 flex items-center gap-1.5 cursor-pointer">
                    <i class="fa-solid fa-graduation-cap text-amber-400"></i> Student Portal
                </button>
                <button type="button" onclick="navigateTo('admin')" id="admin-nav-btn" class="px-3.5 py-1.5 bg-slate-900 border border-slate-800 hover:border-slate-700 text-xs font-bold rounded-lg transition text-amber-400 flex items-center gap-1.5 shadow cursor-pointer">
                    <i class="fa-solid fa-user-shield"></i> Admin Portal
                </button>
            </div>
        </div>
    </header>

    <!-- VIEWS ENGINE CONTAINER -->
    <main class="relative z-10 max-w-6xl mx-auto px-6 py-8 w-full flex-grow flex flex-col justify-center">
        
        <!-- 1. LANDING / APK DOWNLOAD VIEW -->
        <div id="view-landing" class="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            <div class="lg:col-span-7 space-y-8">
                <div class="space-y-4">
                    <div class="inline-flex items-center gap-2 px-3 py-1 bg-amber-500/10 border border-amber-500/20 rounded-full text-amber-400 text-xs font-bold tracking-wider uppercase">
                        ✨ Premium Academy Suite
                    </div>
                    <h1 class="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight leading-none text-white">
                        Master CSS, PMS & PCS Exams
                    </h1>
                    <p class="text-lg sm:text-xl font-bold bg-gradient-to-r from-amber-400 to-rose-400 bg-clip-text text-transparent">
                        Pakistan's Advanced Exam Prep System
                    </p>
                    <p class="text-slate-400 leading-relaxed text-sm sm:text-base">
                        Get elite offline resources, interactive daily testing, constitutional guides, past papers, and our intelligent AI tutor companion. Access instantly from Chrome, PC, Mac, or install the native Android Mobile App!
                    </p>
                </div>

                <div class="flex flex-col sm:flex-row gap-3">
                    <a href="/download" download="CSS_Compass.apk" class="group relative flex items-center justify-between p-4 bg-gradient-to-r from-amber-500 to-rose-600 hover:from-amber-400 hover:to-rose-500 text-slate-950 font-black rounded-xl shadow-xl shadow-orange-500/10 hover:scale-[1.01] transition-all duration-200">
                        <div class="flex items-center space-x-3.5">
                            <i class="fa-solid fa-cloud-arrow-down text-2xl animate-bounce"></i>
                            <div class="text-left">
                                <div class="text-sm font-extrabold tracking-tight">Download Android App</div>
                                <div class="text-[10px] font-bold text-slate-950/70">Uncorrupted APK Direct (20 MB)</div>
                            </div>
                        </div>
                    </a>

                    <button type="button" onclick="navigateTo('student')" class="flex items-center justify-center gap-2.5 p-4 bg-slate-900 hover:bg-slate-850 border border-slate-800 hover:border-slate-700 text-white font-extrabold rounded-xl transition cursor-pointer">
                        <i class="fa-solid fa-graduation-cap text-amber-400 text-lg"></i>
                        <span class="text-sm">Student Portal</span>
                    </button>

                    <button type="button" onclick="navigateTo('admin')" class="flex items-center justify-center gap-2.5 p-4 bg-slate-900 hover:bg-slate-850 border border-slate-800 hover:border-slate-700 text-amber-400 font-extrabold rounded-xl transition cursor-pointer">
                        <i class="fa-solid fa-user-shield text-amber-400 text-lg"></i>
                        <span class="text-sm">Admin Portal</span>
                    </button>
                </div>

                <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
                    <div onclick="navigateTo('student')" class="p-3.5 glass-panel rounded-xl text-center cursor-pointer hover:border-amber-500/40 hover:bg-slate-900/80 transition">
                        <i class="fa-solid fa-atom text-rose-500 text-lg mb-1.5"></i>
                        <p class="font-extrabold text-white">Interactive MCQs</p>
                        <p class="text-[10px] text-slate-400 mt-1">Real-time corrections and syllabus explanations.</p>
                    </div>
                    <div onclick="navigateTo('student')" class="p-3.5 glass-panel rounded-xl text-center cursor-pointer hover:border-amber-500/40 hover:bg-slate-900/80 transition">
                        <i class="fa-solid fa-brain text-amber-400 text-lg mb-1.5"></i>
                        <p class="font-extrabold text-white">Personal AI Coach</p>
                        <p class="text-[10px] text-slate-400 mt-1">Gemini AI Tutor trained for competitive essays.</p>
                    </div>
                    <div onclick="navigateTo('student')" class="p-3.5 glass-panel rounded-xl text-center cursor-pointer hover:border-amber-500/40 hover:bg-slate-900/80 transition">
                        <i class="fa-solid fa-file-invoice text-emerald-400 text-lg mb-1.5"></i>
                        <p class="font-extrabold text-white">Subject Notes</p>
                        <p class="text-[10px] text-slate-400 mt-1">Ready study summaries and solved past papers.</p>
                    </div>
                </div>
            </div>

            <div class="lg:col-span-5">
                <div class="glass-panel p-8 rounded-2xl relative overflow-hidden flex flex-col items-center justify-center text-center shadow-2xl">
                    <div class="absolute -top-12 -right-12 w-32 h-32 bg-amber-500/5 rounded-full blur-2xl"></div>
                    <div class="p-5 bg-gradient-to-b from-slate-900 to-slate-950 border border-slate-800 rounded-xl shadow-inner mb-6">
                        <div class="w-20 h-20 bg-gradient-to-br from-amber-400 via-rose-500 to-rose-600 rounded-full flex items-center justify-center shadow-lg shadow-rose-500/15">
                            <i class="fa-solid fa-compass text-slate-950 text-4xl"></i>
                        </div>
                    </div>
                    <h3 class="text-xl font-black text-white tracking-tight">CSS COMPASS</h3>
                    <p class="text-[10px] text-slate-500 mt-1 code-font">com.aistudio.csscompass</p>
                    <div class="mt-6 flex gap-2 text-[10px]">
                        <span class="px-3 py-1 bg-slate-900 border border-slate-800 rounded-full text-slate-400 font-semibold">Android 7.0+</span>
                        <span class="px-3 py-1 bg-slate-900 border border-slate-800 rounded-full text-slate-400 font-semibold">APK Signed</span>
                        <span class="px-3 py-1 bg-slate-900 border border-slate-800 rounded-full text-emerald-400 font-semibold">v1.0 Ready</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 2. STUDENT LOGIN VIEW -->
        <div id="view-student-login" class="hidden max-w-md mx-auto w-full">
            <div class="glass-panel p-8 rounded-2xl shadow-xl">
                <div class="text-center space-y-2 mb-6">
                    <div class="w-12 h-12 bg-amber-500/10 border border-amber-500/20 rounded-full flex items-center justify-center mx-auto text-amber-400">
                        <i class="fa-solid fa-graduation-cap text-lg"></i>
                    </div>
                    <h2 class="text-2xl font-black text-white">Student Login</h2>
                    <p class="text-xs text-slate-400">Log in to open your CSS Exam Preparation Companion</p>
                </div>

                <form onsubmit="handleLogin(event, 'student')" class="space-y-4" autocomplete="off">
                    <div>
                        <label class="block text-xs font-semibold text-slate-400 mb-1">Email Address or Username</label>
                        <div class="relative">
                            <i class="fa-solid fa-user absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 text-xs"></i>
                            <input type="text" id="student-email" required autocomplete="username" class="w-full bg-slate-900/60 border border-slate-800 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none rounded-xl py-2.5 pl-10 pr-4 text-xs text-slate-200 transition" placeholder="Username or Email (e.g. zaheer)">
                        </div>
                    </div>

                    <div>
                        <label class="block text-xs font-semibold text-slate-400 mb-1">Your Access Password</label>
                        <div class="relative flex items-center">
                            <i class="fa-solid fa-key absolute left-3.5 text-slate-500 text-xs"></i>
                            <input type="password" id="student-password" required autocomplete="current-password" class="w-full bg-slate-900/60 border border-slate-800 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none rounded-xl py-2.5 pl-10 pr-10 text-xs text-slate-200 transition" placeholder="••••••••">
                            <button type="button" onclick="togglePasswordVisibility('student-password', 'student-pass-eye')" class="absolute right-3 text-slate-400 hover:text-white transition cursor-pointer p-1">
                                <i class="fa-solid fa-eye text-xs" id="student-pass-eye"></i>
                            </button>
                        </div>
                    </div>

                    <button type="submit" class="w-full py-2.5 bg-gradient-to-r from-amber-500 to-rose-600 hover:from-amber-400 hover:to-rose-500 text-slate-950 text-xs font-black rounded-xl shadow-lg transition cursor-pointer">
                        LOG IN NOW <i class="fa-solid fa-right-to-bracket ml-1.5"></i>
                    </button>
                </form>

                <div class="mt-4 p-3.5 bg-slate-900/70 border border-slate-800 rounded-xl space-y-2 text-left">
                    <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Suggested First Login Student Credentials:</p>
                    <div class="grid grid-cols-1 gap-2">
                        <button type="button" onclick="fillStudentDemo('ali.css@gmail.com', 'Pakistan123!')" class="py-2.5 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 text-amber-400 font-bold text-[11px] rounded-lg transition text-center flex flex-col items-center justify-center cursor-pointer">
                            <span>⚡ Default Activated Student</span>
                            <span class="text-[9px] font-mono text-slate-300 font-normal mt-0.5">Email / Username: ali.css@gmail.com (or ali_css2026) | Pass: Pakistan123!</span>
                        </button>
                    </div>
                </div>

                <div class="mt-6 pt-4 border-t border-slate-900 text-center">
                    <p class="text-[10px] text-slate-500">Unregistered or unpaid? Contact the administration below to get a premium activated account.</p>
                    <a href="https://wa.me/923001234567?text=Hello%20Admin%2C%20I%20have%20installed%20CSS%20Compass%20and%20would%20like%20to%20register%20or%20complete%20payment%20for%20my%20premium%20account." id="contact-admin-btn" target="_blank" rel="noopener noreferrer" class="mt-3.5 inline-flex items-center gap-1.5 px-4 py-2 bg-slate-900 border border-slate-800 rounded-lg text-xs font-bold text-amber-400 hover:border-slate-700 transition cursor-pointer">
                        <i class="fa-brands fa-whatsapp text-emerald-500"></i> Contact Admin via WhatsApp
                    </a>
                </div>
            </div>
        </div>

        <!-- 3. ADMIN LOGIN VIEW -->
        <div id="view-admin-login" class="hidden max-w-md mx-auto w-full">
            <div class="glass-panel p-8 rounded-2xl shadow-xl">
                <div class="text-center space-y-2 mb-6">
                    <div class="w-12 h-12 bg-amber-500/10 border border-amber-500/20 rounded-full flex items-center justify-center mx-auto text-amber-400">
                        <i class="fa-solid fa-user-shield text-lg"></i>
                    </div>
                    <h2 class="text-2xl font-black text-white">Admin Portal Secure Login</h2>
                    <p class="text-xs text-slate-400">Verify credentials to manage students and Firebase system sync</p>
                </div>

                <form onsubmit="handleLogin(event, 'admin')" class="space-y-4">
                    <div>
                        <label class="block text-xs font-semibold text-slate-400 mb-1">Username / ID</label>
                        <input type="text" id="admin-username" required class="w-full bg-slate-900/60 border border-slate-800 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none rounded-xl py-2.5 px-4 text-xs text-slate-200 transition" placeholder="admin">
                    </div>

                    <div>
                        <label class="block text-xs font-semibold text-slate-400 mb-1">Password</label>
                        <div class="relative flex items-center">
                            <input type="password" id="admin-password" required class="w-full bg-slate-900/60 border border-slate-800 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none rounded-xl py-2.5 pl-4 pr-10 text-xs text-slate-200 transition" placeholder="••••••••">
                            <button type="button" onclick="togglePasswordVisibility('admin-password', 'admin-pass-eye')" class="absolute right-3 text-slate-400 hover:text-white transition cursor-pointer p-1">
                                <i class="fa-solid fa-eye text-xs" id="admin-pass-eye"></i>
                            </button>
                        </div>
                    </div>

                    <button type="submit" class="w-full py-2.5 bg-gradient-to-r from-amber-500 to-rose-600 hover:from-amber-400 hover:to-rose-500 text-slate-950 text-xs font-black rounded-xl shadow-lg transition">
                        AUTHENTICATE SECURELY <i class="fa-solid fa-lock-open ml-1.5"></i>
                    </button>
                </form>

                <div class="mt-4 p-3.5 bg-slate-900/70 border border-slate-800 rounded-xl space-y-2 text-left">
                    <div class="flex justify-between items-center text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                        <span>Default Admin Access</span>
                        <span class="text-amber-400">admin / csscompass2026</span>
                    </div>
                    <button type="button" onclick="fillAdminDemo('admin', 'csscompass2026')" class="w-full py-2 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 text-amber-400 font-bold text-[11px] rounded-lg transition text-center">
                        ⚡ Quick Fill Admin Credentials
                    </button>
                </div>
            </div>
        </div>

        <!-- 4. STUDENT PENDING/UNPAID SCREEN -->
        <div id="view-unpaid-lock" class="hidden max-w-md mx-auto w-full">
            <div class="glass-panel p-8 rounded-2xl text-center border-rose-500/20">
                <div class="w-16 h-16 bg-rose-500/10 border border-rose-500/20 rounded-full flex items-center justify-center mx-auto text-rose-500 animate-pulse mb-4">
                    <i class="fa-solid fa-receipt text-2xl animate-spin-slow"></i>
                </div>
                <h3 class="text-xl font-black text-rose-400">Account Pending Activation</h3>
                <p class="text-xs text-slate-300 mt-2 leading-relaxed">
                    Thank you for registering on CSS Compass. Your student account has been successfully created, but is currently **pending payment** validation.
                </p>
                
                <div class="my-6 p-4 bg-slate-900/50 border border-slate-800 rounded-xl space-y-2.5 text-left text-xs">
                    <p class="text-slate-400 font-medium">To unlock your premium studies companion immediately:</p>
                    <div class="flex items-center gap-2 text-white font-bold"><i class="fa-solid fa-check text-emerald-400"></i> Fee Contribution: PKR 1,500 only</div>
                    <div class="flex items-center gap-2 text-white font-bold"><i class="fa-solid fa-check text-emerald-400"></i> Full Access: All Mock Exams, Solved Papers & AI Coach</div>
                    <div class="flex items-center gap-2 text-white font-bold"><i class="fa-solid fa-check text-emerald-400"></i> Easy Transfer: Bank Account / Easypaisa</div>
                </div>

                <div class="space-y-3">
                    <a href="https://wa.me/923001234567?text=Assalam-o-Alaikum%20Admin%2C%20I%20have%20made%20the%20fee%20payment%20of%20PKR%201%2C500%20for%20CSS%20Compass.%20Please%20activate%20my%20account." id="send-receipt-btn" target="_blank" rel="noopener noreferrer" class="flex items-center justify-center gap-2 w-full py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-extrabold rounded-xl shadow transition cursor-pointer">
                        <i class="fa-brands fa-whatsapp text-lg"></i> SHARE RECEIPT VIA WHATSAPP
                    </a>
                    <button onclick="logoutStudent()" class="w-full text-xs font-bold text-slate-500 hover:text-slate-300 transition cursor-pointer">Log Out Account</button>
                </div>
            </div>
        </div>

        <!-- 5. STUDENT DASHBOARD (WEB APPS COMPANION) -->
        <div id="view-student-dashboard" class="hidden w-full flex flex-col space-y-6">
            <!-- Student Banner -->
            <div class="glass-panel p-6 rounded-2xl flex flex-col md:flex-row justify-between items-center gap-4 border-amber-500/15 bg-gradient-to-r from-slate-950 via-slate-900/50 to-slate-950">
                <div class="text-center md:text-left">
                    <p class="text-[10px] text-amber-400 font-extrabold uppercase tracking-widest">Active Premium Aspirant</p>
                    <h2 class="text-2xl font-black text-white" id="welcome-student-name">Welcome Back</h2>
                    <p class="text-xs text-slate-400">Accessing full desktop companion study suite in Google Chrome</p>
                </div>
                <div class="flex items-center gap-3">
                    <span class="px-3 py-1.5 bg-emerald-500/15 border border-emerald-500/25 text-emerald-400 rounded-lg text-xs font-extrabold flex items-center gap-1.5 uppercase shadow-inner">
                        <i class="fa-solid fa-circle-check"></i> Account Verified
                    </span>
                    <button onclick="logoutStudent()" class="px-3.5 py-1.5 bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-lg text-xs font-bold text-rose-400 transition flex items-center gap-1.5">
                        <i class="fa-solid fa-right-from-bracket"></i> Sign Out
                    </button>
                </div>
            </div>

            <!-- Main Interactive Tabs Grid -->
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
                
                <!-- Left Nav sidebar -->
                <div class="lg:col-span-3 flex flex-col gap-2 bg-slate-900/40 p-3 rounded-2xl border border-slate-900">
                    <button type="button" onclick="switchStudentTab('notes')" id="btn-tab-notes" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-xs font-bold transition text-left bg-amber-500 text-slate-950 cursor-pointer">
                        <i class="fa-solid fa-book-open"></i> CSS Syllabus Notes
                    </button>
                    <button type="button" onclick="switchStudentTab('mcq')" id="btn-tab-mcq" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-xs font-bold transition text-left text-slate-300 hover:bg-slate-900 cursor-pointer">
                        <i class="fa-solid fa-list-check"></i> Interactive MCQs Practice
                    </button>
                    <button type="button" onclick="switchStudentTab('coach')" id="btn-tab-coach" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-xs font-bold transition text-left text-slate-300 hover:bg-slate-900 cursor-pointer">
                        <i class="fa-solid fa-robot text-rose-400"></i> Ask AI Study Coach
                    </button>
                    <button type="button" onclick="switchStudentTab('security')" id="btn-tab-security" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-xs font-bold transition text-left text-slate-300 hover:bg-slate-900 cursor-pointer">
                        <i class="fa-solid fa-key"></i> Password & Device
                    </button>
                    <a href="/download" download="CSS_Compass.apk" class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-xs font-bold transition text-left text-amber-400 hover:bg-slate-900 border-t border-slate-900 mt-2 cursor-pointer">
                        <i class="fa-solid fa-circle-down"></i> Get Android App
                    </a>
                </div>

                <!-- Right Workspace Window -->
                <div class="lg:col-span-9 glass-panel p-6 rounded-2xl min-h-[480px]">
                    
                    <!-- TAB: NOTES -->
                    <div id="student-tab-notes" class="space-y-6">
                        <div class="space-y-1 border-b border-slate-900 pb-4">
                            <h3 class="text-xl font-extrabold text-white">Syllabus Study Notes & Solved Summaries</h3>
                            <p class="text-xs text-slate-400">Premium high-yield academic syllabus reviews written by elite CSS mentors.</p>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <!-- Card 1 -->
                            <div class="bg-slate-900/50 border border-slate-800 p-5 rounded-xl space-y-3 flex flex-col justify-between">
                                <div class="space-y-1.5">
                                    <span class="px-2.5 py-0.5 bg-amber-400/10 text-amber-400 text-[10px] font-extrabold rounded">Pakistan Affairs</span>
                                    <h4 class="text-sm font-bold text-white">The Ideology of Pakistan & Two-Nation Theory</h4>
                                    <p class="text-xs text-slate-400">Historical, political, and philosophical foundations of the creation of Pakistan, including role of Sir Syed Ahmed Khan and Lahore Resolution.</p>
                                </div>
                                <button onclick="readNote('pak-affairs')" class="w-full py-2 bg-slate-950 hover:bg-slate-900 border border-slate-800 text-xs font-bold text-amber-400 rounded-lg transition mt-2">
                                    Open Detailed Notes
                                </button>
                            </div>

                            <!-- Card 2 -->
                            <div class="bg-slate-900/50 border border-slate-800 p-5 rounded-xl space-y-3 flex flex-col justify-between">
                                <div class="space-y-1.5">
                                    <span class="px-2.5 py-0.5 bg-rose-400/10 text-rose-400 text-[10px] font-extrabold rounded">Current Affairs</span>
                                    <h4 class="text-sm font-bold text-white">Pakistan's IMF Reform Programs & Stabilizations</h4>
                                    <p class="text-xs text-slate-400">Complete analysis of fiscal reforms, taxing structural adjustments, circular energy debt deficits, and monetary policy guidelines.</p>
                                </div>
                                <button onclick="readNote('current-affairs')" class="w-full py-2 bg-slate-950 hover:bg-slate-900 border border-slate-800 text-xs font-bold text-amber-400 rounded-lg transition mt-2">
                                    Open Detailed Notes
                                </button>
                            </div>
                        </div>

                        <!-- Notes Reader (Hidden by default, shown on select) -->
                        <div id="notes-reader-container" class="hidden p-5 bg-slate-950 border border-slate-800 rounded-xl space-y-4">
                            <div class="flex justify-between items-center border-b border-slate-900 pb-3">
                                <h4 id="notes-reader-title" class="text-base font-extrabold text-white">Note Title</h4>
                                <button onclick="closeNoteReader()" class="text-xs text-slate-400 hover:text-white transition font-bold">Close <i class="fa-solid fa-xmark ml-1"></i></button>
                            </div>
                            <div id="notes-reader-body" class="text-slate-300 text-xs leading-relaxed space-y-3 custom-scrollbar max-h-96 overflow-y-auto"></div>
                        </div>
                    </div>

                    <!-- TAB: MCQS -->
                    <div id="student-tab-mcq" class="hidden space-y-6">
                        <div class="space-y-1 border-b border-slate-900 pb-4">
                            <h3 class="text-xl font-extrabold text-white">Interactive MCQs Simulator</h3>
                            <p class="text-xs text-slate-400">Practice with random syllabus queries, get immediate feedback, and review books cited.</p>
                        </div>

                        <!-- Active MCQ Box -->
                        <div class="bg-slate-900/50 border border-slate-850 p-6 rounded-xl space-y-5" id="mcq-sim-box">
                            <!-- Topic tag -->
                            <div class="flex items-center justify-between">
                                <span class="px-2.5 py-0.5 bg-slate-800 text-amber-400 text-[10px] font-extrabold rounded uppercase tracking-wider" id="mcq-subject-tag">Pakistan Affairs</span>
                                <span class="text-[10px] text-slate-500 font-bold" id="mcq-ref-tag">Book Ref: Hamid Khan</span>
                            </div>

                            <!-- Question text -->
                            <p class="text-sm font-bold text-white" id="mcq-question-text">Which amendment to the 1973 Constitution of Pakistan abolished the Concurrent List, enhancing provincial autonomy?</p>

                            <!-- Options -->
                            <div class="grid grid-cols-1 gap-3" id="mcq-options-container">
                                <button onclick="selectMcqOption(0)" class="option-btn w-full text-left p-3 bg-slate-950 hover:bg-slate-900 border border-slate-800 hover:border-slate-700 text-xs text-slate-300 font-bold rounded-xl transition">A. 17th Amendment</button>
                                <button onclick="selectMcqOption(1)" class="option-btn w-full text-left p-3 bg-slate-950 hover:bg-slate-900 border border-slate-800 hover:border-slate-700 text-xs text-slate-300 font-bold rounded-xl transition">B. 18th Amendment</button>
                                <button onclick="selectMcqOption(2)" class="option-btn w-full text-left p-3 bg-slate-950 hover:bg-slate-900 border border-slate-800 hover:border-slate-700 text-xs text-slate-300 font-bold rounded-xl transition">C. 19th Amendment</button>
                                <button onclick="selectMcqOption(3)" class="option-btn w-full text-left p-3 bg-slate-950 hover:bg-slate-900 border border-slate-800 hover:border-slate-700 text-xs text-slate-300 font-bold rounded-xl transition">D. 21st Amendment</button>
                            </div>

                            <!-- Explanation Feedback -->
                            <div id="mcq-feedback-box" class="hidden p-4 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-xl space-y-1.5">
                                <p class="text-xs font-extrabold" id="mcq-feedback-title"><i class="fa-solid fa-circle-check mr-1.5"></i> Correct Option!</p>
                                <p class="text-[11px] text-slate-300 leading-relaxed" id="mcq-explanation-text">Explanation goes here.</p>
                            </div>

                            <!-- Controller button -->
                            <div class="flex justify-end pt-2">
                                <button onclick="loadNextMcq()" class="px-5 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 text-xs font-black rounded-lg transition shadow">
                                    Next MCQ Question <i class="fa-solid fa-arrow-right-long ml-1.5"></i>
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- TAB: COACH -->
                    <div id="student-tab-coach" class="hidden flex flex-col h-full justify-between">
                        <div class="space-y-1 border-b border-slate-900 pb-4 mb-4">
                            <h3 class="text-xl font-extrabold text-white">Ask AI Study Coach</h3>
                            <p class="text-xs text-slate-400">Interact with our 24/7 Gemini-powered AI mentor to outline essay drafts, test arguments, and analyze core history articles.</p>
                        </div>

                        <!-- Chat stream -->
                        <div id="chat-messages-stream" class="flex-grow space-y-4 custom-scrollbar max-h-[300px] overflow-y-auto mb-4 p-2">
                            <!-- Bot Welcome -->
                            <div class="flex items-start gap-3">
                                <div class="w-8 h-8 rounded-full bg-amber-500/10 border border-amber-500/25 flex items-center justify-center text-amber-400 shrink-0">
                                    <i class="fa-solid fa-robot text-xs"></i>
                                </div>
                                <div class="p-3 bg-slate-900/60 border border-slate-850 rounded-2xl rounded-tl-none text-xs text-slate-300 max-w-[85%] leading-relaxed">
                                    Assalam-o-Alaikum! I am your AI Study Coach. Ask me any question on Islamic studies, political history, English grammar, or ask me to review a CSS essay draft! How can I guide your preparation today?
                                </div>
                            </div>
                        </div>

                        <!-- Typing Input -->
                        <div class="flex items-center gap-2 border-t border-slate-900 pt-4">
                            <input type="text" id="chat-user-input" onkeydown="handleChatKeyDown(event)" class="flex-grow bg-slate-900/70 border border-slate-800 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 outline-none rounded-xl py-2.5 px-4 text-xs text-slate-200 transition" placeholder="Type your CSS study query (e.g. outline an essay on climate change)...">
                            <button onclick="sendChatMessage()" id="chat-send-btn" class="px-5 py-2.5 bg-gradient-to-r from-amber-500 to-rose-600 hover:from-amber-400 hover:to-rose-500 text-slate-950 font-black rounded-xl text-xs transition shadow flex items-center gap-1.5">
                                SEND <i class="fa-solid fa-paper-plane"></i>
                            </button>
                        </div>
                    </div>

                    <!-- TAB: SECURITY -->
                    <div id="student-tab-security" class="hidden space-y-6">
                        <div class="space-y-1 border-b border-slate-900 pb-4 mb-4">
                            <h3 class="text-xl font-extrabold text-white">Account Credentials & Security Settings</h3>
                            <p class="text-xs text-slate-400">Update your username, email, or password and view bound hardware identifiers.</p>
                        </div>

                        <form id="change-password-form" onsubmit="changeStudentPassword(event)" class="space-y-4 max-w-md">
                            <div class="space-y-1">
                                <label class="text-xs font-bold text-slate-400">Username / Email Identifier</label>
                                <input type="text" id="change-student-username" class="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-xl px-4 py-2.5 text-xs text-white outline-none transition" placeholder="e.g. ali_css2026 or ali.css@gmail.com">
                            </div>

                            <div class="space-y-1">
                                <label class="text-xs font-bold text-slate-400">Current Password (Required to authorize changes)</label>
                                <div class="relative flex items-center">
                                    <input type="password" id="change-password-old" required class="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-xl px-4 pr-10 py-2.5 text-xs text-white outline-none transition" placeholder="Enter your current password">
                                    <button type="button" onclick="togglePasswordVisibility('change-password-old', 'old-pass-eye')" class="absolute right-3 text-slate-400 hover:text-white transition cursor-pointer p-1">
                                        <i class="fa-solid fa-eye text-xs" id="old-pass-eye"></i>
                                    </button>
                                </div>
                            </div>
                            <div class="space-y-1">
                                <label class="text-xs font-bold text-slate-400">New Password (Optional - leave blank if keeping same)</label>
                                <div class="relative flex items-center">
                                    <input type="password" id="change-password-new" class="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-xl px-4 pr-10 py-2.5 text-xs text-white outline-none transition" placeholder="Min. 6 characters">
                                    <button type="button" onclick="togglePasswordVisibility('change-password-new', 'new-pass-eye')" class="absolute right-3 text-slate-400 hover:text-white transition cursor-pointer p-1">
                                        <i class="fa-solid fa-eye text-xs" id="new-pass-eye"></i>
                                    </button>
                                </div>
                            </div>
                            <div class="space-y-1">
                                <label class="text-xs font-bold text-slate-400">Confirm New Password</label>
                                <div class="relative flex items-center">
                                    <input type="password" id="change-password-confirm" class="w-full bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-xl px-4 pr-10 py-2.5 text-xs text-white outline-none transition" placeholder="Re-enter new password">
                                    <button type="button" onclick="togglePasswordVisibility('change-password-confirm', 'confirm-pass-eye')" class="absolute right-3 text-slate-400 hover:text-white transition cursor-pointer p-1">
                                        <i class="fa-solid fa-eye text-xs" id="confirm-pass-eye"></i>
                                    </button>
                                </div>
                            </div>
                            <button type="submit" class="px-5 py-2.5 bg-amber-500 hover:bg-amber-400 transition text-slate-950 text-xs font-black rounded-xl cursor-pointer">
                                Update Student Credentials <i class="fa-solid fa-user-pen ml-1"></i>
                            </button>
                        </form>

                        <div class="border-t border-slate-900 pt-6 space-y-3">
                            <h4 class="text-sm font-bold text-white">Hardware Binding Association</h4>
                            <div class="p-4 bg-slate-950/40 border border-slate-900 rounded-xl space-y-2">
                                <div class="flex flex-col sm:flex-row sm:justify-between sm:items-center text-xs gap-1">
                                    <span class="text-slate-400">Associated Device ID:</span>
                                    <span id="student-bound-device-id" class="font-mono text-amber-400 bg-amber-400/5 px-2 py-0.5 rounded text-[11px] break-all">None (First device will bind on login)</span>
                                </div>
                                <p class="text-[10px] text-slate-500 leading-relaxed">
                                    Device Hardware Binding is active. Your premium account is restricted to the specific physical Android device used during your first app login. If you acquire a new device, please contact the CSS Compass Administrator to request a device association reset.
                                </p>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </div>

        <!-- 6. ADMIN DASHBOARD VIEW -->
        <div id="view-admin-dashboard" class="hidden w-full space-y-6">
            <!-- Header bar -->
            <div class="glass-panel p-6 rounded-2xl flex flex-col sm:flex-row justify-between items-center gap-4">
                <div>
                    <h2 class="text-2xl font-black text-white flex items-center gap-2">
                        <i class="fa-solid fa-user-shield text-amber-400"></i> Admin Management Portal
                    </h2>
                    <p class="text-xs text-slate-400 flex flex-wrap items-center gap-2 mt-1">
                        <span>Control active student databases and Firebase synchronization rules.</span>
                        <span id="admin-user-display" class="hidden px-2 py-0.5 bg-amber-500/10 border border-amber-500/20 text-amber-400 rounded text-[10px] font-mono font-bold"></span>
                    </p>
                </div>
                <button onclick="logoutAdmin()" class="px-3.5 py-1.5 bg-slate-900 border border-slate-800 hover:border-slate-700 text-xs font-bold text-rose-400 rounded-lg transition">
                    Sign Out Admin
                </button>
            </div>

            <!-- Double Column layout -->
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
                
                <!-- Left Column: Add Student and Settings -->
                <div class="lg:col-span-4 space-y-6">
                    
                    <!-- Card: Add Student -->
                    <div class="glass-panel p-5 rounded-xl space-y-4">
                        <h3 class="text-sm font-extrabold text-white border-b border-slate-900 pb-2 flex items-center gap-2">
                            <i class="fa-solid fa-user-plus text-amber-400"></i> Add Student / Customer
                        </h3>

                        <form onsubmit="handleAddStudent(event)" class="space-y-3">
                            <div>
                                <label class="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Full Name</label>
                                <input type="text" id="add-std-name" required class="w-full bg-slate-950 border border-slate-850 focus:border-amber-500 outline-none rounded-lg p-2 text-xs text-slate-200">
                            </div>
                            <div>
                                <label class="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Email Address</label>
                                <input type="email" id="add-std-email" required oninput="handleEmailInput(this.value)" class="w-full bg-slate-950 border border-slate-850 focus:border-amber-500 outline-none rounded-lg p-2 text-xs text-slate-200">
                            </div>
                            <div>
                                <label class="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Username (Required for Android App Login)</label>
                                <input type="text" id="add-std-username" required class="w-full bg-slate-950 border border-slate-850 focus:border-amber-500 outline-none rounded-lg p-2 text-xs text-slate-200" placeholder="e.g. css_aspirant2026">
                            </div>
                            <div>
                                <label class="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">WhatsApp / Phone</label>
                                <input type="text" id="add-std-phone" class="w-full bg-slate-950 border border-slate-850 focus:border-amber-500 outline-none rounded-lg p-2 text-xs text-slate-200" placeholder="+923001234567">
                            </div>
                            <div>
                                <label class="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Student Password</label>
                                <div class="flex gap-1.5">
                                    <input type="text" id="add-std-pass" required class="flex-grow bg-slate-950 border border-slate-850 focus:border-amber-500 outline-none rounded-lg p-2 text-xs text-slate-200">
                                    <button type="button" onclick="generateRandomPass()" class="px-2 bg-slate-900 hover:bg-slate-850 border border-slate-800 rounded-lg text-xs font-bold text-slate-400"><i class="fa-solid fa-key"></i></button>
                                </div>
                            </div>
                            <div class="flex items-center gap-2 pt-1.5">
                                <input type="checkbox" id="add-std-paid" class="w-4 h-4 rounded bg-slate-950 border-slate-800 text-amber-500 focus:ring-0">
                                <label class="text-xs font-bold text-slate-300 cursor-pointer" for="add-std-paid">Mark as Paid (Premium On)</label>
                            </div>

                            <button type="submit" class="w-full py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 text-xs font-black rounded-lg transition shadow mt-3">
                                SAVE STUDENT ACCOUNT <i class="fa-solid fa-floppy-disk ml-1"></i>
                            </button>
                        </form>
                    </div>

                    <!-- Card: Firebase Configuration -->
                    <div class="glass-panel p-5 rounded-xl space-y-4">
                        <h3 class="text-sm font-extrabold text-white border-b border-slate-900 pb-2 flex items-center gap-2">
                            <i class="fa-brands fa-google text-amber-400"></i> Firebase Sync Panel
                        </h3>

                        <p class="text-[10px] text-slate-400 leading-relaxed">
                            Easily mirror your student accounts and access codes to Google Firebase Realtime Database. Set up rule auth tokens to access endpoints safely.
                        </p>

                        <form onsubmit="handleFirebaseSetup(event)" class="space-y-3">
                            <div>
                                <label class="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Realtime Database URL</label>
                                <input type="url" id="fb-db-url" class="w-full bg-slate-950 border border-slate-850 focus:border-amber-500 outline-none rounded-lg p-2 text-xs text-slate-200 code-font" placeholder="https://your-proj-default-rtdb.firebaseio.com">
                            </div>
                            <div>
                                <label class="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Web Auth Token (API Key)</label>
                                <input type="password" id="fb-api-key" class="w-full bg-slate-950 border border-slate-850 focus:border-amber-500 outline-none rounded-lg p-2 text-xs text-slate-200 code-font" placeholder="••••••••">
                            </div>

                            <div class="grid grid-cols-2 gap-2 pt-2">
                                <button type="submit" onclick="setFirebaseAction('push')" class="py-1.5 bg-slate-900 hover:bg-slate-850 border border-slate-800 text-[10px] font-black text-amber-400 rounded-lg transition">
                                    <i class="fa-solid fa-cloud-arrow-up"></i> PUSH TO FB
                                </button>
                                <button type="submit" onclick="setFirebaseAction('pull')" class="py-1.5 bg-slate-900 hover:bg-slate-850 border border-slate-800 text-[10px] font-black text-amber-400 rounded-lg transition">
                                    <i class="fa-solid fa-cloud-arrow-down"></i> PULL FROM FB
                                </button>
                            </div>
                        </form>
                    </div>

                    <!-- Card: WhatsApp Settings -->
                    <div class="glass-panel p-5 rounded-xl space-y-4">
                        <h3 class="text-sm font-extrabold text-white border-b border-slate-900 pb-2 flex items-center gap-2">
                            <i class="fa-brands fa-whatsapp text-emerald-400"></i> Support WhatsApp Settings
                        </h3>
                        <div class="space-y-3">
                            <div>
                                <label class="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Admin WhatsApp Number</label>
                                <input type="text" id="admin-whatsapp-num" class="w-full bg-slate-950 border border-slate-850 focus:border-amber-500 outline-none rounded-lg p-2 text-xs text-slate-200" placeholder="+923001234567">
                            </div>
                            <button onclick="saveAdminWhatsapp()" class="w-full py-1.5 bg-slate-900 hover:bg-slate-850 border border-slate-800 text-[10px] font-black text-slate-300 rounded-lg transition">
                                SAVE WHATSAPP INFO
                            </button>
                        </div>
                    </div>

                    <!-- Card: Change Admin Password & Username -->
                    <div class="glass-panel p-5 rounded-xl space-y-4">
                        <h3 class="text-sm font-extrabold text-white border-b border-slate-900 pb-2 flex items-center gap-2">
                            <i class="fa-solid fa-user-shield text-amber-400"></i> Admin Security & Credentials
                        </h3>
                        <form onsubmit="saveAdminSecurity(event)" class="space-y-3">
                            <div>
                                <label class="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">Admin Username</label>
                                <input type="text" id="admin-change-username" required class="w-full bg-slate-950 border border-slate-850 focus:border-amber-500 outline-none rounded-lg p-2 text-xs text-slate-200" placeholder="admin">
                            </div>
                            <div>
                                <label class="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1">New Admin Password</label>
                                <div class="relative flex items-center">
                                    <input type="password" id="admin-change-password" required class="w-full bg-slate-950 border border-slate-850 focus:border-amber-500 outline-none rounded-lg p-2 pr-8 text-xs text-slate-200" placeholder="••••••••">
                                    <button type="button" onclick="togglePasswordVisibility('admin-change-password', 'admin-change-eye')" class="absolute right-2 text-slate-400 hover:text-white cursor-pointer p-1">
                                        <i class="fa-solid fa-eye text-xs" id="admin-change-eye"></i>
                                    </button>
                                </div>
                            </div>
                            <button type="submit" class="w-full py-1.5 bg-amber-500 hover:bg-amber-400 text-slate-950 text-[10px] font-black rounded-lg transition cursor-pointer">
                                UPDATE ADMIN CREDENTIALS <i class="fa-solid fa-shield-halved ml-1"></i>
                            </button>
                        </form>
                    </div>

                </div>

                <!-- Right Column: Student Grid List -->
                <div class="lg:col-span-8 glass-panel p-5 rounded-xl space-y-4">
                    <div class="flex items-center justify-between border-b border-slate-900 pb-3">
                        <h3 class="text-base font-extrabold text-white flex items-center gap-2">
                            <i class="fa-solid fa-users text-amber-400"></i> Registered Student Accounts
                        </h3>
                        <span class="px-2.5 py-1 bg-slate-900 border border-slate-800 rounded-full text-slate-400 text-xs font-bold" id="student-count-badge">Total: 0</span>
                    </div>

                    <div class="overflow-x-auto">
                        <table class="w-full text-left text-xs text-slate-300">
                            <thead class="bg-slate-900/60 uppercase text-[10px] tracking-wider text-slate-400">
                                <tr>
                                    <th class="p-3">Student Name</th>
                                    <th class="p-3">Username</th>
                                    <th class="p-3">Email Address</th>
                                    <th class="p-3">Password</th>
                                    <th class="p-3">Device Bind</th>
                                    <th class="p-3">Status</th>
                                    <th class="p-3 text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody id="student-table-body" class="divide-y divide-slate-900">
                                <!-- Dynamic JS injection -->
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </div>

    </main>

    <!-- FOOTER -->
    <footer class="relative z-10 border-t border-slate-900 py-6 bg-slate-950 text-center">
        <div class="max-w-6xl mx-auto px-6 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
            <p>© 2026 CSS Compass Academy Pakistan. All rights reserved.</p>
            <div class="flex items-center gap-4">
                <span class="flex items-center gap-1"><i class="fa-solid fa-cloud"></i> Firebase Synced</span>
                <span>•</span>
                <span>Designed for commercial distribution</span>
            </div>
        </div>
    </footer>

    <!-- LOGIC SYSTEM SCRIPT -->
    <script>
        let currentStudent = null;
        let isAdmin = false;
        let globalConfig = { whatsappNumber: "+923001234567" };
        let activeFbAction = 'save';
        let activeStudentTab = 'notes';

        // Prepopulated MCQs Database for Web app simulation
        const sampleMcqs = [
            {
                subject: "Pakistan Affairs",
                topic: "Constitutional Development",
                ref: "Hamid Khan Guide",
                question: "Which amendment to the 1973 Constitution of Pakistan abolished the Concurrent List, enhancing provincial autonomy?",
                options: ["17th Amendment", "18th Amendment", "19th Amendment", "21st Amendment"],
                correctIndex: 1,
                explanation: "The 18th Amendment, passed in 2010, abolished the Concurrent Legislative List, delegating greater legislative and financial power directly to the provinces."
            },
            {
                subject: "Pakistan Affairs",
                topic: "Pre-Republic Era",
                ref: "Ian Talbot Guide",
                question: "The objective resolution of Pakistan was passed on which of the following dates?",
                options: ["March 12, 1949", "August 14, 1948", "March 23, 1940", "September 11, 1948"],
                correctIndex: 0,
                explanation: "The Objective Resolution was moved by Liaquat Ali Khan, the first Prime Minister of Pakistan, and approved on March 12, 1949, serving as the foundation of future constitutions."
            },
            {
                subject: "English Précis",
                topic: "Vocabulary Builder",
                ref: "GRE Barron's Guide",
                question: "Select the word closest in meaning to 'Pernicious':",
                options: ["Beneficial", "Harmful", "Anomalous", "Incongruous"],
                correctIndex: 1,
                explanation: "Pernicious means having a harmful, highly damaging, or destructive effect, especially in a gradual or subtle way."
            },
            {
                subject: "Islamiat",
                topic: "Islamic Jurisprudence (Fiqh)",
                ref: "M. Hashim Kamali",
                question: "In Islamic Jurisprudence, 'Ijma' stands for which of the following?",
                options: ["Individual reasoning", "Consensus of opinions", "Analogy of texts", "Traditional customs"],
                correctIndex: 1,
                explanation: "Ijma refers to the consensus of Islamic jurists on a particular legal issue where direct rulings are not explicit in the Quran and Sunnah."
            }
        ];
        let currentMcqIndex = 0;

        const sampleNotes = {
            'pak-affairs': {
                title: "The Ideology of Pakistan & Two-Nation Theory",
                body: "<h3><b>The Two-Nation Theory</b></h3><br><p>The Two-Nation Theory is the foundational ideology of Pakistan. It states that Muslims and Hindus are two distinct nations, with separate religions, cultures, histories, social values, and political philosophies.</p><br><h3><b>Key Milestones</b></h3><br><ul><li><b>Sir Syed Ahmed Khan:</b> Initiated the modernization of Muslim education through the Aligarh Movement. Post-Hindi-Urdu controversy (1867), he recognized that joint coexistence would be challenging.</li><br><li><b>Allama Iqbal's Allahabad Address (1930):</b> Iqbal provided a solid philosophical framework, declaring that a separate Muslim homeland in North-Western India was the destiny of Indian Muslims.</li><br><li><b>Quaid-e-Azam's Address (1940):</b> In his historic Lahore Resolution speech, Quaid-e-Azam declared: \\\"Hindus and Muslims belong to two different religious philosophies, social customs, and literatures... They neither intermarry nor interdine...\\\"</li></ul>"
            },
            'current-affairs': {
                title: "Pakistan's IMF Program & Economic Stabilization",
                body: "<h3><b>Pakistan and the IMF</b></h3><br><p>Pakistan's economic challenges stem from structural imbalances, including fiscal deficits, a low tax-to-GDP ratio, and circular debt in the power sector.</p><br><h3><b>Key Reforms Mandated by IMF</b></h3><br><ul><li><b>Tax Reforms:</b> Expanding the tax net, raising direct taxes, and digitalizing the Federal Board of Revenue (FBR).</li><br><li><b>Energy Tariffs:</b> Cost-reflective energy tariffs to curb circular debt.</li><br><li><b>Monetary Policy:</b> Maintaining a tight monetary stance to anchor inflation expectations.</li><br><li><b>Privatization:</b> Restructuring State-Owned Enterprises (SOEs) such as PIA and steel mills.</li></ul>"
            }
        };

        function fillStudentDemo(email, password) {
            document.getElementById('student-email').value = email;
            document.getElementById('student-password').value = password;
            performLogin(email, password, 'student');
        }

        function fillAdminDemo(username, password) {
            document.getElementById('admin-username').value = username;
            document.getElementById('admin-password').value = password;
            performLogin(username, password, 'admin');
        }

        function restoreSession() {
            try {
                const storedAdmin = localStorage.getItem('css_isAdmin');
                if (storedAdmin === 'true') {
                    isAdmin = true;
                    const adminDisplay = document.getElementById('admin-user-display');
                    if (adminDisplay) {
                        adminDisplay.innerText = "Logged in: Admin";
                        adminDisplay.classList.remove('hidden');
                    }
                }
                const storedStudent = localStorage.getItem('css_student');
                if (storedStudent) {
                    currentStudent = JSON.parse(storedStudent);
                    if (currentStudent && currentStudent.isPaid) {
                        const nameEl = document.getElementById('welcome-student-name');
                        if (nameEl) nameEl.innerText = "Assalam-o-Alaikum, " + currentStudent.name + "!";
                        const boundEl = document.getElementById('student-bound-device-id');
                        if (boundEl) {
                            boundEl.innerText = currentStudent.deviceId || "None (First app login will bind device)";
                        }
                    }
                }
            } catch(e) {
                console.error("Error restoring session:", e);
            }
        }

        let activeTargetView = 'landing';

        function safeSetStorage(key, val) {
            try { sessionStorage.setItem(key, val); } catch(e){}
            try { localStorage.setItem(key, val); } catch(e){}
        }

        function safeGetStorage(key) {
            try {
                return sessionStorage.getItem(key) || localStorage.getItem(key);
            } catch(e) {
                return null;
            }
        }

        function checkRoute() {
            try {
                const hash = (window.location.hash || '').toLowerCase();
                const search = (window.location.search || '').toLowerCase();

                if (hash.includes('admin') || search.includes('admin')) {
                    navigateTo('admin');
                } else if (hash.includes('student') || search.includes('student')) {
                    navigateTo('student');
                } else if (hash.includes('home') || hash.includes('landing')) {
                    navigateTo('landing');
                } else {
                    const savedView = safeGetStorage('css_active_view') || activeTargetView || 'landing';
                    navigateTo(savedView);
                }
            } catch(e) {
                console.error("checkRoute error:", e);
                navigateTo(activeTargetView || 'landing');
            }
        }

        function initApp() {
            restoreSession();
            fetchConfig();
            checkRoute();
            window.addEventListener('hashchange', checkRoute);
        }

        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', initApp);
        } else {
            initApp();
        }

        function updateActiveNav(viewId) {
            const homeBtn = document.getElementById('home-nav-btn');
            const studentBtn = document.getElementById('student-nav-btn');
            const adminBtn = document.getElementById('admin-nav-btn');

            if (homeBtn) {
                if (viewId === 'landing') {
                    homeBtn.className = "text-xs font-bold text-amber-400 bg-amber-500/10 border border-amber-500/20 py-1.5 px-3 rounded-lg transition cursor-pointer";
                } else {
                    homeBtn.className = "text-xs font-bold text-slate-400 hover:text-white transition py-1.5 px-3 rounded-lg hover:bg-slate-900 cursor-pointer";
                }
            }

            if (studentBtn) {
                if (viewId === 'student-login' || viewId === 'student-dashboard' || viewId === 'unpaid-lock') {
                    studentBtn.className = "text-xs font-bold text-amber-400 bg-amber-500/10 border border-amber-500/20 py-1.5 px-3 rounded-lg transition flex items-center gap-1.5 cursor-pointer";
                } else {
                    studentBtn.className = "text-xs font-bold text-slate-400 hover:text-white transition py-1.5 px-3 rounded-lg hover:bg-slate-900 flex items-center gap-1.5 cursor-pointer";
                }
            }

            if (adminBtn) {
                if (viewId === 'admin-login' || viewId === 'admin-dashboard') {
                    adminBtn.className = "px-3.5 py-1.5 bg-amber-500/20 border border-amber-500/40 text-xs font-bold rounded-lg transition text-amber-300 flex items-center gap-1.5 shadow-lg shadow-amber-500/10 cursor-pointer";
                } else {
                    adminBtn.className = "px-3.5 py-1.5 bg-slate-900 border border-slate-800 hover:border-slate-700 text-xs font-bold rounded-lg transition text-amber-400 flex items-center gap-1.5 shadow cursor-pointer";
                }
            }
        }

        // View Navigator - Pure DOM View Toggling with !important Display Overrides
        function navigateTo(targetView) {
            try {
                if (!targetView) targetView = 'landing';

                let viewId = targetView;

                // Resolve student/admin views according to auth state
                if (targetView.startsWith('student') || targetView === 'unpaid-lock') {
                    if (currentStudent) {
                        viewId = currentStudent.isPaid ? 'student-dashboard' : 'unpaid-lock';
                    } else {
                        viewId = 'student-login';
                    }
                } else if (targetView.startsWith('admin')) {
                    if (isAdmin) {
                        viewId = 'admin-dashboard';
                    } else {
                        viewId = 'admin-login';
                    }
                } else {
                    viewId = 'landing';
                }

                activeTargetView = viewId;
                safeSetStorage('css_active_view', viewId);

                const views = ['landing', 'student-login', 'admin-login', 'unpaid-lock', 'student-dashboard', 'admin-dashboard'];
                views.forEach(v => {
                    const el = document.getElementById('view-' + v);
                    if (el) {
                        if (v === viewId) {
                            el.classList.remove('hidden');
                            el.style.setProperty('display', (v === 'landing') ? 'grid' : 'block', 'important');
                        } else {
                            el.classList.add('hidden');
                            el.style.setProperty('display', 'none', 'important');
                        }
                    }
                });

                // Scroll to top
                window.scrollTo(0, 0);

                updateActiveNav(viewId);

                // Update URL history state cleanly
                try {
                    let newHash = '#home';
                    if (viewId === 'admin-login' || viewId === 'admin-dashboard') {
                        newHash = '#admin';
                    } else if (viewId === 'student-login' || viewId === 'student-dashboard' || viewId === 'unpaid-lock') {
                        newHash = '#student';
                    }

                    if (window.location.hash !== newHash) {
                        if (history && history.replaceState) {
                            history.replaceState(null, '', newHash);
                        } else {
                            window.location.hash = newHash;
                        }
                    }
                } catch(e) {}

                if (viewId === 'admin-dashboard') {
                    fetchStudents();
                }
            } catch(e) {
                console.error("navigateTo error:", e);
            }
        }

        function formatPhoneForWa(p) {
            if (!p) return "";
            let digits = String(p).replace(/\D/g, '');
            if (digits.startsWith('0')) {
                digits = '92' + digits.substring(1);
            }
            return digits;
        }

        function updateWhatsappLinks() {
            try {
                const waNum = (globalConfig && globalConfig.whatsappNumber) ? globalConfig.whatsappNumber : "+923001234567";
                const formatted = formatPhoneForWa(waNum);
                const contactBtn = document.getElementById('contact-admin-btn');
                if (contactBtn) {
                    const supportMsg = encodeURIComponent("Hello Admin, I have installed CSS Compass and would like to register or complete payment for my premium account.");
                    contactBtn.href = "https://wa.me/" + formatted + "?text=" + supportMsg;
                }
                const receiptBtn = document.getElementById('send-receipt-btn');
                if (receiptBtn) {
                    const studentEmail = (currentStudent && currentStudent.email) ? currentStudent.email : "my student account";
                    const receiptMsg = encodeURIComponent("Assalam-o-Alaikum Admin, I have made the fee payment of PKR 1,500 for CSS Compass. Please activate my account for: " + studentEmail);
                    receiptBtn.href = "https://wa.me/" + formatted + "?text=" + receiptMsg;
                }
            } catch(e) {}
        }

        function openWhatsappContact(event) {
            updateWhatsappLinks();
        }

        function openWhatsappReceipt(event) {
            updateWhatsappLinks();
        }

        function fetchConfig() {
            fetch('/api/config')
                .then(res => res.json())
                .then(config => {
                    if (!config) return;
                    globalConfig = config;
                    const numEl = document.getElementById('admin-whatsapp-num');
                    if (numEl && config.whatsappNumber) numEl.value = config.whatsappNumber;
                    const adminUserEl = document.getElementById('admin-change-username');
                    if (adminUserEl && config.adminUsername) adminUserEl.value = config.adminUsername;
                    updateWhatsappLinks();
                })
                .catch(err => console.error("Error loading config:", err));
        }

        // Handle Logins
        function performLogin(email, password, role) {
            fetch('/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            })
            .then(res => {
                if (!res.ok) {
                    return res.json().then(d => { throw new Error(d.message || "Authentication failed") });
                }
                return res.json();
            })
            .then(data => {
                if (data.isAdmin) {
                    isAdmin = true;
                    try { localStorage.setItem('css_isAdmin', 'true'); } catch(e){}
                    const adminDisplay = document.getElementById('admin-user-display');
                    if (adminDisplay) {
                        adminDisplay.innerText = "Logged in: " + email;
                        adminDisplay.classList.remove('hidden');
                    }
                    navigateTo('admin-dashboard');
                } else {
                    currentStudent = data.student;
                    try { localStorage.setItem('css_student', JSON.stringify(data.student)); } catch(e){}
                    if (currentStudent.isPaid) {
                        document.getElementById('welcome-student-name').innerText = "Assalam-o-Alaikum, " + currentStudent.name + "!";
                        const boundEl = document.getElementById('student-bound-device-id');
                        if (boundEl) {
                            boundEl.innerText = currentStudent.deviceId || "None (First app login will bind device)";
                        }
                        navigateTo('student-dashboard');
                        loadMcq();
                    } else {
                        // Unpaid account locked
                        const payMsg = encodeURIComponent("Assalam-o-Alaikum Admin, I have made the fee payment of PKR 1,500 for CSS Compass. Please activate my account for: " + currentStudent.email);
                        document.getElementById('send-receipt-btn').href = "https://wa.me/" + formatPhoneForWa(globalConfig.whatsappNumber) + "?text=" + payMsg;
                        navigateTo('unpaid-lock');
                    }
                }
            })
            .catch(err => {
                alert(err.message);
            });
        }

        function handleLogin(event, role) {
            event.preventDefault();
            const email = (role === 'student') ? document.getElementById('student-email').value : document.getElementById('admin-username').value;
            const password = (role === 'student') ? document.getElementById('student-password').value : document.getElementById('admin-password').value;
            performLogin(email, password, role);
        }

        // Student Log Out
        function logoutStudent() {
            currentStudent = null;
            try { localStorage.removeItem('css_student'); } catch(e){}
            document.getElementById('student-email').value = "";
            document.getElementById('student-password').value = "";
            navigateTo('student-login');
        }

        // Admin Log Out
        function logoutAdmin() {
            isAdmin = false;
            try { localStorage.removeItem('css_isAdmin'); } catch(e){}
            document.getElementById('admin-username').value = "";
            document.getElementById('admin-password').value = "";
            const adminDisplay = document.getElementById('admin-user-display');
            if (adminDisplay) {
                adminDisplay.innerText = "";
                adminDisplay.classList.add('hidden');
            }
            navigateTo('landing');
        }

        // Switch Tabs inside Student Workspace
        function switchStudentTab(tab) {
            activeStudentTab = tab;
            const tabs = ['notes', 'mcq', 'coach', 'security'];
            tabs.forEach(t => {
                const el = document.getElementById('student-tab-' + t);
                const btn = document.getElementById('btn-tab-' + t);
                if (el) el.classList.add('hidden');
                if (btn) btn.className = "w-full flex items-center gap-3 px-4 py-3 rounded-xl text-xs font-bold transition text-left text-slate-300 hover:bg-slate-900";
            });
            const activeEl = document.getElementById('student-tab-' + tab);
            const activeBtn = document.getElementById('btn-tab-' + tab);
            if (activeEl) activeEl.classList.remove('hidden');
            if (activeBtn) activeBtn.className = "w-full flex items-center gap-3 px-4 py-3 rounded-xl text-xs font-bold transition text-left bg-amber-500 text-slate-950";
        }

        // Study Notes tab functions
        function readNote(noteId) {
            const note = sampleNotes[noteId];
            if (note) {
                document.getElementById('notes-reader-title').innerText = note.title;
                document.getElementById('notes-reader-body').innerHTML = note.body;
                document.getElementById('notes-reader-container').classList.remove('hidden');
            }
        }
        function closeNoteReader() {
            document.getElementById('notes-reader-container').classList.add('hidden');
        }

        // MCQ practicing functions
        function loadMcq() {
            const mcq = sampleMcqs[currentMcqIndex];
            document.getElementById('mcq-subject-tag').innerText = mcq.subject + " • " + mcq.topic;
            document.getElementById('mcq-ref-tag').innerText = "Book: " + mcq.ref;
            document.getElementById('mcq-question-text').innerText = mcq.question;
            
            const container = document.getElementById('mcq-options-container');
            container.innerHTML = "";
            
            mcq.options.forEach((opt, idx) => {
                const btn = document.createElement('button');
                btn.className = "option-btn w-full text-left p-3.5 bg-slate-950 hover:bg-slate-900 border border-slate-800 hover:border-slate-700 text-xs text-slate-300 font-bold rounded-xl transition";
                btn.innerText = String.fromCharCode(65 + idx) + ". " + opt;
                btn.onclick = () => selectMcqOption(idx);
                container.appendChild(btn);
            });

            document.getElementById('mcq-feedback-box').classList.add('hidden');
        }

        function selectMcqOption(selectedIdx) {
            const mcq = sampleMcqs[currentMcqIndex];
            const feedbackBox = document.getElementById('mcq-feedback-box');
            const feedbackTitle = document.getElementById('mcq-feedback-title');
            const explanationText = document.getElementById('mcq-explanation-text');
            const optionBtns = document.getElementsByClassName('option-btn');

            // Visual markers for all buttons
            for (let i = 0; i < optionBtns.length; i++) {
                optionBtns[i].disabled = true;
                if (i === mcq.correctIndex) {
                    optionBtns[i].className = "option-btn w-full text-left p-3.5 bg-emerald-950/40 border border-emerald-500 text-xs text-emerald-400 font-black rounded-xl";
                } else if (i === selectedIdx) {
                    optionBtns[i].className = "option-btn w-full text-left p-3.5 bg-rose-950/40 border border-rose-500 text-xs text-rose-400 font-black rounded-xl";
                }
            }

            feedbackBox.classList.remove('hidden');
            if (selectedIdx === mcq.correctIndex) {
                feedbackBox.className = "p-4 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-xl space-y-1.5";
                feedbackTitle.innerHTML = '<i class="fa-solid fa-circle-check mr-1.5"></i> Correct Option!';
            } else {
                feedbackBox.className = "p-4 bg-rose-500/10 border border-rose-500/20 text-rose-400 rounded-xl space-y-1.5";
                feedbackTitle.innerHTML = '<i class="fa-solid fa-circle-xmark mr-1.5"></i> Incorrect Answer';
            }
            explanationText.innerText = mcq.explanation;
        }

        function loadNextMcq() {
            currentMcqIndex = (currentMcqIndex + 1) % sampleMcqs.length;
            loadMcq();
        }

        // Gemini AI chat helper
        function sendChatMessage() {
            const inputEl = document.getElementById('chat-user-input');
            const prompt = inputEl.value.trim();
            if (!prompt) return;

            // Append User msg
            const stream = document.getElementById('chat-messages-stream');
            const userMsgDiv = document.createElement('div');
            userMsgDiv.className = "flex items-start gap-3 justify-end";
            userMsgDiv.innerHTML = '<div class="p-3 bg-amber-500 text-slate-950 rounded-2xl rounded-tr-none text-xs font-bold max-w-[85%] leading-relaxed">' + escapeHtml(prompt) + '</div>';
            stream.appendChild(userMsgDiv);
            stream.scrollTop = stream.scrollHeight;

            inputEl.value = "";
            document.getElementById('chat-send-btn').disabled = true;

            // Typing indicator
            const typingEl = document.createElement('div');
            typingEl.className = "flex items-start gap-3";
            typingEl.id = "typing-loader";
            typingEl.innerHTML = '<div class="p-3 bg-slate-900 border border-slate-850 rounded-xl text-xs text-slate-500"><i class="fa-solid fa-circle-notch animate-spin mr-1.5"></i> AI Coach analyzing...</div>';
            stream.appendChild(typingEl);
            stream.scrollTop = stream.scrollHeight;

            fetch('/api/gemini', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ prompt })
            })
            .then(res => res.json())
            .then(data => {
                document.getElementById('typing-loader').remove();
                document.getElementById('chat-send-btn').disabled = false;
                
                // Append AI reply
                const aiReplyDiv = document.createElement('div');
                aiReplyDiv.className = "flex items-start gap-3";
                aiReplyDiv.innerHTML = '<div class="w-8 h-8 rounded-full bg-amber-500/10 border border-amber-500/25 flex items-center justify-center text-amber-400 shrink-0"><i class="fa-solid fa-robot text-xs"></i></div><div class="p-3 bg-slate-900/60 border border-slate-850 rounded-2xl rounded-tl-none text-xs text-slate-300 max-w-[85%] leading-relaxed">' + formatBotResponse(data.reply || data.message || "Response received") + '</div>';
                stream.appendChild(aiReplyDiv);
                stream.scrollTop = stream.scrollHeight;
            })
            .catch(err => {
                document.getElementById('typing-loader').remove();
                document.getElementById('chat-send-btn').disabled = false;
                alert("Gemini error: " + err.message);
            });
        }

        function handleChatKeyDown(event) {
            if (event.key === 'Enter') {
                sendChatMessage();
            }
        }

        function escapeHtml(text) {
            return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
        }

        function formatBotResponse(text) {
            // Basic bold markdown formatter
            return escapeHtml(text)
                .replace(/\\*\\*(.*?)\\*\\*/g, '<b>$1</b>')
                .replace(/\\*(.*?)\\*/g, '<i>$1</i>')
                .replace(/\\n/g, '<br>');
        }

        // --- ADMIN DASHBOARD FUNCTIONS ---

        function fetchStudents() {
            fetch('/api/students')
                .then(res => res.json())
                .then(students => {
                    const badge = document.getElementById('student-count-badge');
                    if (badge) badge.innerText = "Total: " + (students ? students.length : 0);
                    const tbody = document.getElementById('student-table-body');
                    if (!tbody) return;
                    tbody.innerHTML = "";

                    if (!Array.isArray(students)) return;

                    students.forEach(std => {
                        if (!std) return;
                        const tr = document.createElement('tr');
                        tr.className = "hover:bg-slate-900/20";
                        
                        // Prefilled copy credentials msg
                        const baseDomain = window.location.origin;
                        const apkUrl = baseDomain + "/CSS_Compass.apk";
                        const studentUsername = std.username || (std.email ? std.email.split('@')[0] : 'student');
                        const loginInvite = [
                            "Assalam-o-Alaikum " + (std.name || "Aspirant") + "!",
                            "",
                            "Your CSS Compass Premium Account is ready!",
                            "",
                            "📲 Download & Install Android App (APK):",
                            apkUrl,
                            "",
                            "🌐 Web Portal Access (Chrome):",
                            baseDomain,
                            "",
                            "🔑 Login Credentials:",
                            "📧 Email: " + (std.email || ""),
                            "👤 Username: " + studentUsername,
                            "🔒 Password: " + (std.password || ""),
                            "",
                            "Good luck with your PMS/PCS/CSS preparations! 📚✨"
                        ].join(String.fromCharCode(10));
                        const whatsappLink = "https://wa.me/" + formatPhoneForWa(std.phone || "") + "?text=" + encodeURIComponent(loginInvite);

                        const deviceCell = std.deviceId ? 
                            '<span class="font-mono text-[10px] text-amber-400 bg-amber-400/5 px-2 py-0.5 border border-amber-500/10 rounded mr-1" title="' + std.deviceId + '">' + std.deviceId.substring(0, 12) + '...</span><button onclick="resetDevice(\'' + std.id + '\')" title="Reset Device Bind" class="p-1 bg-slate-950 border border-slate-800 text-rose-400 hover:text-rose-300 rounded text-[9px] inline-flex items-center"><i class="fa-solid fa-arrow-rotate-left"></i></button>' : 
                            '<span class="text-slate-500 text-[10px]">None Bound</span>';

                        const paidCell = std.isPaid ? 
                            '<span class="px-2 py-0.5 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded text-[10px] font-bold">PAID</span>' : 
                            '<span class="px-2 py-0.5 bg-rose-500/10 border border-rose-500/30 text-rose-400 rounded text-[10px] font-bold">UNPAID</span>';

                        const formattedDate = std.createdAt ? new Date(std.createdAt).toLocaleDateString() : 'Recent';

                        tr.innerHTML = 
                            '<td class="p-3">' +
                                '<div class="font-bold text-white">' + (std.name || 'Aspirant') + '</div>' +
                                '<div class="text-[9px] text-slate-500">' + formattedDate + '</div>' +
                            '</td>' +
                            '<td class="p-3 font-mono text-amber-400 font-bold">' + studentUsername + '</td>' +
                            '<td class="p-3 text-slate-300">' + (std.email || '') + '</td>' +
                            '<td class="p-3 text-amber-300 font-mono text-[11px] whitespace-nowrap">' +
                                '<span id="pass-mask-' + std.id + '">••••••••</span> ' +
                                '<button type="button" onclick="toggleAdminStudentPass(\'' + std.id + '\', \'' + (std.password || '').replace(/'/g, "\\'") + '\')" class="text-[10px] text-slate-400 hover:text-amber-400 p-0.5 cursor-pointer">' +
                                    '<i class="fa-solid fa-eye text-[10px]" id="pass-icon-' + std.id + '"></i>' +
                                '</button>' +
                            '</td>' +
                            '<td class="p-3 whitespace-nowrap">' + deviceCell + '</td>' +
                            '<td class="p-3">' + paidCell + '</td>' +
                            '<td class="p-3 text-right space-x-1.5 whitespace-nowrap">' +
                                '<a href="' + whatsappLink + '" target="_blank" title="Share via WhatsApp" class="p-1.5 bg-slate-900 hover:bg-slate-850 text-emerald-400 hover:text-emerald-300 border border-slate-800 rounded transition text-[11px] font-bold inline-flex items-center gap-1">' +
                                    '<i class="fa-brands fa-whatsapp"></i> Send Invitation' +
                                '</a> ' +
                                '<button onclick="deleteStudent(\'' + std.id + '\')" title="Delete Student" class="p-1.5 bg-slate-900 hover:bg-slate-850 text-rose-500 hover:text-rose-400 border border-slate-800 rounded transition text-[11px]">' +
                                    '<i class="fa-solid fa-trash-can"></i>' +
                                '</button>' +
                            '</td>';
                        tbody.appendChild(tr);
                    });
                })
                .catch(err => console.error("Error loading students:", err));
        }

        function generateRandomPass() {
            const random = Math.floor(100000 + Math.random() * 900000);
            document.getElementById('add-std-pass').value = "CSS-" + random;
        }

        function handleEmailInput(val) {
            const usernameInput = document.getElementById('add-std-username');
            if (usernameInput) {
                const currentVal = usernameInput.value.trim();
                const prefix = val.split('@')[0].toLowerCase().replace(/[^a-z0-9_]/g, '');
                if (!currentVal || currentVal === usernameInput.dataset.autoFilled) {
                    usernameInput.value = prefix;
                    usernameInput.dataset.autoFilled = prefix;
                }
            }
        }

        function handleAddStudent(event) {
            event.preventDefault();
            const name = document.getElementById('add-std-name').value.trim();
            const email = document.getElementById('add-std-email').value.trim();
            const username = document.getElementById('add-std-username').value.trim();
            const phone = document.getElementById('add-std-phone').value.trim();
            const password = document.getElementById('add-std-pass').value.trim();
            const isPaid = document.getElementById('add-std-paid').checked;

            fetch('/api/students', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, username, phone, password, isPaid })
            })
            .then(res => res.json())
            .then(data => {
                alert("Student saved successfully!");
                // Clear fields
                document.getElementById('add-std-name').value = "";
                document.getElementById('add-std-email').value = "";
                document.getElementById('add-std-username').value = "";
                document.getElementById('add-std-phone').value = "";
                document.getElementById('add-std-pass').value = "";
                document.getElementById('add-std-paid').checked = false;
                
                fetchStudents();
            })
            .catch(err => alert("Error saving student: " + err.message));
        }

        function deleteStudent(id) {
            if (!confirm("Are you sure you want to remove this student account?")) return;
            fetch('/api/students?id=' + id, { method: 'DELETE' })
                .then(res => res.json())
                .then(data => {
                    alert("Student deleted successfully!");
                    fetchStudents();
                })
                .catch(err => alert("Deletion failed: " + err.message));
        }

        function resetDevice(id) {
            if (!confirm("Are you sure you want to reset this student's bound device? This will allow them to register a new phone on their next login.")) return;
            fetch('/api/students/reset-device?id=' + id, { method: 'POST' })
                .then(res => res.json())
                .then(data => {
                    alert("Device reset successfully!");
                    fetchStudents();
                })
                .catch(err => alert("Reset device failed: " + err.message));
        }

        function changeStudentPassword(event) {
            event.preventDefault();
            const newUsernameInput = document.getElementById('change-student-username').value.trim();
            const oldPassword = document.getElementById('change-password-old').value;
            const newPassword = document.getElementById('change-password-new').value;
            const confirmPassword = document.getElementById('change-password-confirm').value;

            if (newPassword && newPassword.length < 4) {
                alert("New password must be at least 4 characters long.");
                return;
            }

            if (newPassword && newPassword !== confirmPassword) {
                alert("New passwords do not match.");
                return;
            }

            const payload = {
                email: currentStudent.email,
                oldPassword: oldPassword
            };
            if (newPassword) payload.newPassword = newPassword;
            if (newUsernameInput) {
                if (newUsernameInput.includes('@')) {
                    payload.newEmail = newUsernameInput;
                } else {
                    payload.newUsername = newUsernameInput;
                }
            }

            fetch('/api/change-password', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    alert("Student credentials updated successfully!");
                    if (data.student) {
                        currentStudent = data.student;
                        try { localStorage.setItem('css_student', JSON.stringify(data.student)); } catch(e){}
                    }
                    document.getElementById('change-student-username').value = "";
                    document.getElementById('change-password-old').value = "";
                    document.getElementById('change-password-new').value = "";
                    document.getElementById('change-password-confirm').value = "";
                } else {
                    alert(data.message || "Failed to update credentials.");
                }
            })
            .catch(err => {
                alert("Error changing credentials: " + err.message);
            });
        }

        function setFirebaseAction(action) {
            activeFbAction = action;
        }

        function handleFirebaseSetup(event) {
            event.preventDefault();
            const databaseUrl = document.getElementById('fb-db-url').value.trim();
            const apiKey = document.getElementById('fb-api-key').value.trim();

            if (!databaseUrl) {
                alert("Please enter a valid Firebase Database URL first");
                return;
            }

            fetch('/api/firebase', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ databaseUrl, apiKey, action: activeFbAction })
            })
            .then(res => res.json())
            .then(data => {
                alert(data.message);
                if (activeFbAction === 'pull') {
                    fetchStudents();
                }
            })
            .catch(err => alert("Firebase Sync failed: " + err.message));
        }

        function saveAdminWhatsapp() {
            const whatsappNumber = document.getElementById('admin-whatsapp-num').value.trim();
            fetch('/api/config', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ whatsappNumber })
            })
            .then(res => res.json())
            .then(data => {
                alert("WhatsApp Support saved successfully!");
                fetchConfig();
            })
            .catch(err => alert("Failed saving config: " + err.message));
        }

        function togglePasswordVisibility(inputId, iconId) {
            const input = document.getElementById(inputId);
            const icon = document.getElementById(iconId);
            if (!input || !icon) return;
            if (input.type === "password") {
                input.type = "text";
                icon.classList.remove("fa-eye");
                icon.classList.add("fa-eye-slash");
            } else {
                input.type = "password";
                icon.classList.remove("fa-eye-slash");
                icon.classList.add("fa-eye");
            }
        }

        function toggleAdminStudentPass(id, actualPass) {
            const el = document.getElementById('pass-mask-' + id);
            const icon = document.getElementById('pass-icon-' + id);
            if (!el || !icon) return;
            if (el.innerText === '••••••••') {
                el.innerText = actualPass;
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                el.innerText = '••••••••';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        }

        function saveAdminSecurity(event) {
            event.preventDefault();
            const adminUsername = document.getElementById('admin-change-username').value.trim();
            const adminPassword = document.getElementById('admin-change-password').value.trim();

            if (!adminUsername || !adminPassword) {
                alert("Username and password cannot be empty");
                return;
            }

            fetch('/api/config', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ adminUsername, adminPassword })
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    alert("Admin credentials updated successfully!");
                    document.getElementById('admin-change-password').value = "";
                    fetchConfig();
                } else {
                    alert(data.message || "Failed to update admin credentials.");
                }
            })
            .catch(err => alert("Failed saving admin security: " + err.message));
        }
    </script>
</body>
</html>`;
}
