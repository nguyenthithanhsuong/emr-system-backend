const API_BASE = 'http://localhost:8080/api';

const state = {
  users: [],
  details: [],
};

const form = document.getElementById('userForm');
const roleButtons = document.querySelectorAll('.role-btn');
const dynamicLabel = document.getElementById('dynamicLabel');
const dynamicInput = document.getElementById('specialtyOrCondition');
const resultCard = document.getElementById('resultCard');
const resultRole = document.getElementById('resultRole');
const resultId = document.getElementById('resultId');
const resultName = document.getElementById('resultName');
const resultMeta = document.getElementById('resultMeta');
const secondaryTableLabel = document.getElementById('secondaryTableLabel');
const usersTable = document.getElementById('usersTable');
const detailTable = document.getElementById('detailTable');
const steps = document.querySelectorAll('#steps li');

let selectedRole = 'Doctor';

async function fetchSnapshot() {
  const response = await fetch(`${API_BASE}/snapshot`);
  if (!response.ok) {
    throw new Error('Cannot load snapshot from backend.');
  }
  return response.json();
}

async function registerUser(formData) {
  const payload = new URLSearchParams();
  payload.set('userType', selectedRole);
  payload.set('fullName', formData.fullName);
  payload.set('specialtyOrCondition', formData.specialtyOrCondition);

  const response = await fetch(`${API_BASE}/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
    },
    body: payload.toString(),
  });

  const result = await response.json();
  if (!response.ok) {
    throw new Error(result.error || 'Register request failed.');
  }

  return result;
}

function updateRoleUI(role) {
  selectedRole = role;

  roleButtons.forEach((button) => {
    button.classList.toggle('active', button.dataset.role === role);
  });

  if (role === 'Doctor') {
    dynamicLabel.firstChild.textContent = ' Chuyên khoa';
    dynamicInput.placeholder = 'Tim mạch';
    dynamicInput.value = dynamicInput.value || 'Tim mạch';
    secondaryTableLabel.textContent = 'Bảng Bác sĩ';
  } else {
    dynamicLabel.firstChild.textContent = ' Tình trạng bệnh';
    dynamicInput.placeholder = 'Tiểu đường';
    dynamicInput.value = dynamicInput.value || 'Tiểu đường';
    secondaryTableLabel.textContent = 'Bảng Bệnh nhân';
  }
}

function animateSteps() {
  steps.forEach((step, index) => {
    step.classList.toggle('active', index === 0);
  });

  setTimeout(() => {
    steps[1].classList.add('active');
  }, 120);

  setTimeout(() => {
    steps[2].classList.add('active');
  }, 240);

  setTimeout(() => {
    steps[3].classList.add('active');
  }, 360);
}

function renderTables() {
  usersTable.innerHTML = state.users
    .map((user) => `
      <div class="row">
        <span>${String(user.id).padStart(3, '0')}</span>
        <span>${user.fullName}</span>
        <span><span class="role-pill">${user.role}</span></span>
      </div>
    `)
    .join('');

  detailTable.innerHTML = state.details
    .map((item) => `
      <div class="row">
        <span>${String(item.id).padStart(3, '0')}</span>
        <span>${item.type}</span>
        <span class="detail-pill">${item.detail}</span>
      </div>
    `)
    .join('');
}

function showLatestUser(user, formData) {
  resultRole.textContent = user.role;
  resultId.textContent = `#${String(user.id).padStart(3, '0')}`;
  resultName.textContent = user.fullName;
  resultMeta.textContent = formData.specialtyOrCondition;
  resultCard.classList.remove('flash');
  void resultCard.offsetWidth;
  resultCard.classList.add('flash');
}

roleButtons.forEach((button) => {
  button.addEventListener('click', () => {
    updateRoleUI(button.dataset.role);
  });
});

form.addEventListener('submit', async (event) => {
  event.preventDefault();

  const formData = {
    fullName: document.getElementById('fullName').value.trim(),
    specialtyOrCondition: document.getElementById('specialtyOrCondition').value.trim(),
  };

  try {
    const user = await registerUser(formData);
    const snapshot = await fetchSnapshot();
    state.users = snapshot.users || [];
    state.details = snapshot.details || [];

    animateSteps();
    showLatestUser(user, formData);
    renderTables();
    form.reset();
    updateRoleUI(selectedRole);
  } catch (error) {
    alert(error.message);
  }
});

async function initialize() {
  updateRoleUI(selectedRole);
  try {
    const snapshot = await fetchSnapshot();
    state.users = snapshot.users || [];
    state.details = snapshot.details || [];
    renderTables();
  } catch (error) {
    alert('Khong ket noi duoc backend Java. Hay chay App.java truoc.');
  }
}

initialize();
