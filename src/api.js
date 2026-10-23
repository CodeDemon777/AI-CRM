// API client for Spring Boot Backend
// When hosted on Vercel, set VITE_API_URL (e.g., https://ai-crm-backend.onrender.com)
// In local development, leave empty to use the Vite proxy to http://localhost:8081
const envApiUrl = (import.meta.env.VITE_API_URL || '').trim().replace(/\/$/, '');
const API_BASE = envApiUrl
  ? (envApiUrl.endsWith('/api') ? envApiUrl : `${envApiUrl}/api`)
  : '/api';

export async function checkBackendHealth() {
  try {
    const res = await fetch(`${API_BASE}/health`, {
      headers: { Accept: 'application/json' },
    });
    if (!res.ok) return { connected: false };
    const contentType = res.headers.get('content-type') || '';
    if (!contentType.includes('application/json')) {
      return { connected: false };
    }
    return await res.json();
  } catch {
    return { connected: false };
  }
}

export async function fetchCustomers(search = '') {
  const url = search ? `${API_BASE}/customers?search=${encodeURIComponent(search)}` : `${API_BASE}/customers`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('Failed to fetch customers from backend');
  return await res.json();
}

export async function createCustomerApi(customer) {
  const res = await fetch(`${API_BASE}/customers`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(customer),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Failed to create customer');
  }
  return await res.json();
}

export async function deleteCustomerApi(id) {
  const res = await fetch(`${API_BASE}/customers/${id}`, {
    method: 'DELETE',
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Failed to delete customer');
  }
  return await res.json();
}

export async function fetchLeads(customerId = '', status = '') {
  const params = new URLSearchParams();
  if (customerId) params.append('customerId', customerId);
  if (status) params.append('status', status);
  const query = params.toString() ? `?${params.toString()}` : '';

  const res = await fetch(`${API_BASE}/leads${query}`);
  if (!res.ok) throw new Error('Failed to fetch leads from backend');
  return await res.json();
}

export async function createLeadApi(lead) {
  const res = await fetch(`${API_BASE}/leads`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(lead),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Failed to create lead');
  }
  return await res.json();
}

export async function updateLeadStatusApi(id, status) {
  const res = await fetch(`${API_BASE}/leads/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Failed to update lead status');
  }
  return await res.json();
}

export async function deleteLeadApi(id) {
  const res = await fetch(`${API_BASE}/leads/${id}`, {
    method: 'DELETE',
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.message || 'Failed to delete lead');
  }
  return await res.json();
}

export async function fetchAiInsights() {
  const res = await fetch(`${API_BASE}/ai/insights`);
  if (!res.ok) throw new Error('Failed to fetch AI insights');
  return await res.json();
}

export async function fetchDashboardStats() {
  const res = await fetch(`${API_BASE}/dashboard/stats`);
  if (!res.ok) throw new Error('Failed to fetch dashboard stats');
  return await res.json();
}
