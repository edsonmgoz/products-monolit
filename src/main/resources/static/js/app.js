'use strict';

const API = '/api/products';

// ─── State ───────────────────────────────────────────────────────────────────
let allProducts = [];
let pendingDeleteId = null;

// ─── DOM refs ─────────────────────────────────────────────────────────────────
const tbody          = document.getElementById('products-body');
const searchInput    = document.getElementById('search');
const modalOverlay   = document.getElementById('modal-overlay');
const confirmOverlay = document.getElementById('confirm-overlay');
const productForm    = document.getElementById('product-form');
const modalTitle     = document.getElementById('modal-title');
const toastContainer = document.getElementById('toast-container');

const fieldId          = document.getElementById('product-id');
const fieldName        = document.getElementById('field-name');
const fieldDescription = document.getElementById('field-description');
const fieldPrice       = document.getElementById('field-price');
const fieldStock       = document.getElementById('field-stock');
const fieldActive      = document.getElementById('field-active');

const errName        = document.getElementById('err-name');
const errDescription = document.getElementById('err-description');
const errPrice       = document.getElementById('err-price');
const errStock       = document.getElementById('err-stock');

// ─── API helpers ──────────────────────────────────────────────────────────────
async function apiFetch(path, options = {}) {
  const res = await fetch(path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (res.status === 204) return null;
  const body = await res.json().catch(() => null);
  if (!res.ok) throw { status: res.status, body };
  return body;
}

// ─── Load & render ────────────────────────────────────────────────────────────
async function loadProducts() {
  try {
    allProducts = await apiFetch(API);
    renderTable(allProducts);
    renderStats(allProducts);
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No se pudieron cargar los productos.</td></tr>`;
    toast('Error al cargar los productos', 'error');
  }
}

function renderStats(products) {
  const active = products.filter(p => p.active).length;
  document.getElementById('stat-total').textContent    = products.length;
  document.getElementById('stat-active').textContent   = active;
  document.getElementById('stat-inactive').textContent = products.length - active;
}

function renderTable(products) {
  if (products.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No se encontraron productos.</td></tr>`;
    return;
  }
  tbody.innerHTML = products.map(p => `
    <tr data-id="${p.id}">
      <td>${p.id}</td>
      <td><strong>${esc(p.name)}</strong></td>
      <td class="description"><span>${esc(p.description || '—')}</span></td>
      <td class="price">$${Number(p.price).toFixed(2)}</td>
      <td class="stock">${p.stock}</td>
      <td><span class="badge ${p.active ? 'badge-active' : 'badge-inactive'}">${p.active ? 'Activo' : 'Inactivo'}</span></td>
      <td>
        <div class="actions">
          <button class="btn-icon" title="Editar" onclick="openEdit(${p.id})">&#9998;</button>
          <button class="btn-icon danger" title="Eliminar" onclick="openConfirmDelete(${p.id}, '${esc(p.name)}')">&#128465;</button>
        </div>
      </td>
    </tr>
  `).join('');
}

// ─── Search ───────────────────────────────────────────────────────────────────
searchInput.addEventListener('input', () => {
  const q = searchInput.value.toLowerCase().trim();
  if (!q) { renderTable(allProducts); return; }
  renderTable(allProducts.filter(p =>
    p.name.toLowerCase().includes(q) ||
    (p.description || '').toLowerCase().includes(q)
  ));
});

// ─── Modal open/close ─────────────────────────────────────────────────────────
document.getElementById('btn-new').addEventListener('click', openCreate);
document.getElementById('modal-close').addEventListener('click', closeModal);
document.getElementById('btn-cancel').addEventListener('click', closeModal);
modalOverlay.addEventListener('click', e => { if (e.target === modalOverlay) closeModal(); });

document.getElementById('btn-confirm-cancel').addEventListener('click', closeConfirm);
confirmOverlay.addEventListener('click', e => { if (e.target === confirmOverlay) closeConfirm(); });

function openCreate() {
  clearForm();
  modalTitle.textContent = 'Nuevo producto';
  fieldId.value = '';
  fieldActive.checked = true;
  modalOverlay.classList.add('open');
  fieldName.focus();
}

function openEdit(id) {
  const p = allProducts.find(x => x.id === id);
  if (!p) return;
  clearForm();
  modalTitle.textContent = 'Editar producto';
  fieldId.value          = p.id;
  fieldName.value        = p.name;
  fieldDescription.value = p.description || '';
  fieldPrice.value       = p.price;
  fieldStock.value       = p.stock;
  fieldActive.checked    = p.active;
  modalOverlay.classList.add('open');
  fieldName.focus();
}

function closeModal() {
  modalOverlay.classList.remove('open');
  clearForm();
}

function openConfirmDelete(id, name) {
  pendingDeleteId = id;
  document.getElementById('confirm-name').textContent = name;
  confirmOverlay.classList.add('open');
}

function closeConfirm() {
  pendingDeleteId = null;
  confirmOverlay.classList.remove('open');
}

// ─── Form submit ──────────────────────────────────────────────────────────────
productForm.addEventListener('submit', async e => {
  e.preventDefault();
  clearErrors();

  const id = fieldId.value;
  const payload = {
    name:        fieldName.value.trim(),
    description: fieldDescription.value.trim(),
    price:       parseFloat(fieldPrice.value),
    stock:       parseInt(fieldStock.value, 10),
    active:      fieldActive.checked,
  };

  try {
    if (id) {
      await apiFetch(`${API}/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      toast('Producto actualizado correctamente', 'success');
    } else {
      await apiFetch(API, { method: 'POST', body: JSON.stringify(payload) });
      toast('Producto creado correctamente', 'success');
    }
    closeModal();
    await loadProducts();
  } catch (err) {
    if (err.body && err.body.errors) {
      showValidationErrors(err.body.errors);
    } else if (err.body && err.body.message) {
      toast(err.body.message, 'error');
    } else {
      toast('Ocurrió un error inesperado', 'error');
    }
  }
});

// ─── Delete ───────────────────────────────────────────────────────────────────
document.getElementById('btn-confirm-delete').addEventListener('click', async () => {
  if (!pendingDeleteId) return;
  const id = pendingDeleteId;
  closeConfirm();
  try {
    await apiFetch(`${API}/${id}`, { method: 'DELETE' });
    toast('Producto eliminado correctamente', 'success');
    await loadProducts();
  } catch (err) {
    toast('No se pudo eliminar el producto', 'error');
  }
});

// ─── Validation errors ────────────────────────────────────────────────────────
function showValidationErrors(errors) {
  const map    = { name: errName, description: errDescription, price: errPrice, stock: errStock };
  const fields = { name: fieldName, description: fieldDescription, price: fieldPrice, stock: fieldStock };
  Object.entries(errors).forEach(([key, msg]) => {
    if (map[key]) { map[key].textContent = msg; fields[key].classList.add('invalid'); }
  });
}

function clearErrors() {
  [errName, errDescription, errPrice, errStock].forEach(el => { el.textContent = ''; });
  [fieldName, fieldDescription, fieldPrice, fieldStock].forEach(el => el.classList.remove('invalid'));
}

function clearForm() {
  productForm.reset();
  clearErrors();
}

// ─── Toast ────────────────────────────────────────────────────────────────────
function toast(message, type = 'success') {
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.innerHTML = `<span class="toast-icon">${type === 'success' ? '✓' : '✕'}</span><span>${esc(message)}</span>`;
  toastContainer.appendChild(el);
  setTimeout(() => el.remove(), 3500);
}

// ─── Utils ────────────────────────────────────────────────────────────────────
function esc(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

// ─── Init ─────────────────────────────────────────────────────────────────────
loadProducts();
