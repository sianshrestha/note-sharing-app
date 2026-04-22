const API_BASE = 'http://localhost:8080';
let authToken = localStorage.getItem('jwt_token');
let currentUsername = localStorage.getItem('username');

// --- ROUTING & INITIALIZATION ---
window.onload = function() {
    // 1. Capture OAuth2 Token if redirected from backend
    const params = new URLSearchParams(window.location.search);
    const urlToken = params.get('token');
    if (urlToken) {
        authToken = urlToken;
        localStorage.setItem('jwt_token', authToken);
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    // 2. Page Protection Logic
    const isLoginPage = window.location.pathname.endsWith('login.html');

    // If not logged in and not on login page -> Redirect to login
    if (!authToken && !isLoginPage) {
        window.location.href = 'login.html';
        return;
    }
    // If logged in and on login page -> Redirect to home
    if (authToken && isLoginPage) {
        window.location.href = 'index.html';
        return;
    }

    // 3. Initialize Page Specific Features
    if (authToken) {
        setupNavbar();
        if (document.getElementById('notesList')) loadNotes();
        if (document.getElementById('profileDetails')) loadProfile();
        if (document.getElementById('bookmarksList')) loadBookmarks();

        // Event Listeners for Forms
        if (document.getElementById('uploadForm')) document.getElementById('uploadForm').addEventListener('submit', handleUpload);
        if (document.getElementById('searchForm')) document.getElementById('searchForm').addEventListener('submit', handleSearch);
        if (document.getElementById('updateProfileForm')) document.getElementById('updateProfileForm').addEventListener('submit', handleUpdateProfile);
        if (document.getElementById('editNoteForm')) document.getElementById('editNoteForm').addEventListener('submit', handleEditNoteSubmit);
    } else {
        // Only exists on login.html
        if (document.getElementById('loginForm')) document.getElementById('loginForm').addEventListener('submit', handleLogin);
        if (document.getElementById('registerForm')) document.getElementById('registerForm').addEventListener('submit', handleRegister);
    }
};

function setupNavbar() {
    const usernameDisplay = document.getElementById('displayUsername');
    if(usernameDisplay && currentUsername) usernameDisplay.innerText = currentUsername;

    // Highlight active link
    const links = document.querySelectorAll('.nav-links a');
    links.forEach(link => {
        if(window.location.href.includes(link.getAttribute('href'))) {
            link.classList.add('active');
        }
    });
}

function logout() {
    authToken = null;
    currentUsername = null;
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('username');
    window.location.href = 'login.html';
}

// --- API FETCH WRAPPER ---
async function fetchAuth(endpoint, options = {}) {
    options.headers = options.headers || {};
    options.headers['Authorization'] = `Bearer ${authToken}`;

    const response = await fetch(`${API_BASE}${endpoint}`, options);
    if (response.status === 401 || response.status === 403) {
        alert("Session expired. Please login again.");
        logout();
        return null;
    }
    return response;
}

// --- AUTHENTICATION ---
async function handleLogin(e) {
    e.preventDefault();
    const payload = { email: e.target.email.value, password: e.target.password.value };
    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error('Login failed. Check credentials.');
        const data = await res.json();
        localStorage.setItem('jwt_token', data.accessToken);
        window.location.href = 'index.html';
    } catch (err) { alert(err.message); }
}

async function handleRegister(e) {
    e.preventDefault();
    const payload = { username: e.target.username.value, email: e.target.email.value, password: e.target.password.value };
    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error('Registration failed.');
        const data = await res.json();
        localStorage.setItem('jwt_token', data.accessToken);
        alert("Registered successfully!");
        window.location.href = 'index.html';
    } catch (err) { alert(err.message); }
}

// --- USER PROFILE ---
async function loadProfile() {
    const res = await fetchAuth('/users/profile/me');
    if (!res) return;
    const data = await res.json();
    currentUsername = data.username;
    localStorage.setItem('username', currentUsername);

    document.getElementById('profileDetails').innerHTML = `
        <p><strong>Username:</strong> ${data.username}</p>
        <p><strong>Email:</strong> ${data.email}</p>
        <p><strong>Notes Uploaded:</strong> ${data.uploadedNotes ? data.uploadedNotes.length : 0}</p>
        <p><strong>Notes Bookmarked:</strong> ${data.bookmarkedNotes ? data.bookmarkedNotes.length : 0}</p>
    `;
    document.getElementById('displayUsername').innerText = currentUsername;
}

async function handleUpdateProfile(e) {
    e.preventDefault();
    const payload = {
        username: document.getElementById('updateUsername').value || null,
        email: document.getElementById('updateEmail').value || null
    };
    const res = await fetchAuth('/users/profile/me', {
        method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    });
    if (res && res.ok) {
        alert("Profile updated! You will be logged out to refresh your session.");
        logout();
    } else {
        alert("Failed to update profile.");
    }
}

// --- NOTES MANAGEMENT ---
async function loadNotes(queryParams = '') {
    const res = await fetchAuth(`/notes${queryParams}`);
    if (!res) return;
    const page = await res.json();
    const notes = page.content;

    const list = document.getElementById('notesList');
    if (notes.length === 0) { list.innerHTML = '<p>No notes found.</p>'; return; }

    // Make sure we have the username for the edit/delete button check
    if(!currentUsername) currentUsername = localStorage.getItem('username');

    list.innerHTML = notes.map(note => {
        const isOwner = note.uploadedBy === currentUsername;
        return `
        <div class="list-item">
            <div class="list-info">
                <h4>${note.title} <span style="font-weight:normal; font-size:0.8em; color:#666">(${note.fileType})</span></h4>
                <div class="list-meta">
                    <strong>Subject:</strong> ${note.subject || 'N/A'} <br>
                    <strong>Desc:</strong> ${note.description || 'No description'} <br>
                    <strong>By:</strong> ${note.uploadedBy} | <strong>Size:</strong> ${(note.fileSize / 1024).toFixed(1)} KB
                </div>
            </div>
            <div class="list-actions">
                <button onclick="downloadNote(${note.id})" class="success btn-sm">Download</button>
                <button onclick="addBookmark(${note.id})" class="secondary btn-sm">Bookmark</button>
                ${isOwner ? `<button onclick="openEditModal(${note.id}, '${note.title}', '${note.subject}', '${note.description}')" class="btn btn-sm">Edit</button>
                             <button onclick="deleteNote(${note.id})" class="danger btn-sm">Delete</button>` : ''}
            </div>
        </div>
    `}).join('');
}

function handleSearch(e) {
    e.preventDefault();
    const params = new URLSearchParams();
    if (document.getElementById('searchTitle').value) params.append('title', document.getElementById('searchTitle').value);
    if (document.getElementById('searchSubject').value) params.append('subject', document.getElementById('searchSubject').value);
    if (document.getElementById('searchUploader').value) params.append('uploadedBy', document.getElementById('searchUploader').value);
    loadNotes(`?${params.toString()}`);
}

function clearSearch() { document.getElementById('searchForm').reset(); loadNotes(); }

async function handleUpload(e) {
    e.preventDefault();
    const formData = new FormData();
    formData.append('title', document.getElementById('uploadTitle').value);
    formData.append('subject', document.getElementById('uploadSubject').value);
    formData.append('description', document.getElementById('uploadDesc').value);
    formData.append('file', document.getElementById('uploadFile').files[0]);

    const res = await fetchAuth('/notes/upload', { method: 'POST', body: formData });
    if (res && res.ok) {
        alert('Note uploaded successfully!');
        window.location.href = 'index.html'; // Redirect back to notes
    } else {
        alert('Upload failed. Ensure file is PDF, TXT, PNG, or JPG and under 10MB.');
    }
}

async function downloadNote(id) {
    const res = await fetchAuth(`/notes/${id}/download`);
    if (res && res.ok) { const data = await res.json(); window.open(data.downloadUrl, '_blank'); }
}

async function deleteNote(id) {
    if (!confirm('Delete this note?')) return;
    const res = await fetchAuth(`/notes/${id}`, { method: 'DELETE' });
    if (res && res.ok) loadNotes();
}

// --- EDIT NOTE MODAL ---
function openEditModal(id, title, subject, desc) {
    document.getElementById('editNoteId').value = id;
    document.getElementById('editTitle').value = title;
    document.getElementById('editSubject').value = subject !== 'null' ? subject : '';
    document.getElementById('editDesc').value = desc !== 'null' ? desc : '';
    document.getElementById('editNoteModal').classList.remove('hidden');
}

function closeEditModal() { document.getElementById('editNoteModal').classList.add('hidden'); }

async function handleEditNoteSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('editNoteId').value;
    const formData = new FormData();
    formData.append('title', document.getElementById('editTitle').value);
    formData.append('subject', document.getElementById('editSubject').value);
    formData.append('description', document.getElementById('editDesc').value);
    if(document.getElementById('editFile').files.length > 0) formData.append('file', document.getElementById('editFile').files[0]);

    const res = await fetchAuth(`/notes/${id}`, { method: 'PUT', body: formData });
    if (res && res.ok) { alert('Updated!'); closeEditModal(); loadNotes(); }
}

// --- BOOKMARKS MANAGEMENT ---
async function loadBookmarks() {
    const res = await fetchAuth('/bookmarks');
    if (!res) return;
    const bookmarks = await res.json();
    const list = document.getElementById('bookmarksList');
    if (bookmarks.length === 0) { list.innerHTML = '<p>No bookmarks yet.</p>'; return; }

    list.innerHTML = bookmarks.map(bm => `
        <div class="list-item">
            <div class="list-info">
                <h4>${bm.noteTitle}</h4>
                <div class="list-meta">Subject: ${bm.noteSubject || 'N/A'} | By: ${bm.noteUploadedBy}</div>
            </div>
            <div class="list-actions">
                <button onclick="window.open('${bm.downloadUrl}', '_blank')" class="success btn-sm">Download</button>
                <button onclick="removeBookmark(${bm.noteId})" class="danger btn-sm">Remove</button>
            </div>
        </div>
    `).join('');
}

async function addBookmark(noteId) {
    const res = await fetchAuth(`/bookmarks/${noteId}`, { method: 'POST' });
    if (res && res.ok) { alert('Bookmarked!'); } else { alert('Already bookmarked or error occurred.'); }
}

async function removeBookmark(noteId) {
    const res = await fetchAuth(`/bookmarks/${noteId}`, { method: 'DELETE' });
    if (res && res.ok) loadBookmarks();
}