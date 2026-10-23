import { useState, useEffect } from "react";
import "./App.css";
import {
  checkBackendHealth,
  fetchCustomers,
  createCustomerApi,
  deleteCustomerApi,
  fetchLeads,
  createLeadApi,
  updateLeadStatusApi,
} from "./api";

const initialCustomers = [
  {
    id: "1",
    name: "Priya Sharma",
    email: "priya@example.com",
    company: "Bright Solutions",
    phone: "9876543210",
  },
  {
    id: "2",
    name: "Arun Kumar",
    email: "arun@example.com",
    company: "TechNova",
    phone: "9876501234",
  },
];

const initialLeads = [
  {
    id: "101",
    customerId: "1",
    title: "Website Development",
    value: 25000,
    status: "New",
  },
  {
    id: "102",
    customerId: "2",
    title: "CRM Subscription",
    value: 15000,
    status: "Contacted",
  },
];

function App() {
  const [activeTab, setActiveTab] = useState("Dashboard");
  const [customers, setCustomers] = useState(initialCustomers);
  const [leads, setLeads] = useState(initialLeads);
  const [search, setSearch] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [backendStatus, setBackendStatus] = useState({
    connected: false,
    databaseConnected: false,
    databaseMode: "Demo mode (Frontend)",
  });

  const [customerForm, setCustomerForm] = useState({
    name: "",
    email: "",
    company: "",
    phone: "",
  });

  const [leadForm, setLeadForm] = useState({
    customerId: "",
    title: "",
    value: "",
  });

  // Manual refresh handler
  const handleRefresh = async () => {
    clearFeedback();
    try {
      const health = await checkBackendHealth();
      if (health && health.status === "UP") {
        setBackendStatus({
          connected: true,
          databaseConnected: !!health.databaseConnected,
          databaseMode: health.databaseMode || "Spring Boot Active",
        });

        const [serverCustomers, serverLeads] = await Promise.all([
          fetchCustomers().catch(() => null),
          fetchLeads().catch(() => null),
        ]);
        if (serverCustomers && serverCustomers.length > 0) setCustomers(serverCustomers);
        if (serverLeads && serverLeads.length > 0) setLeads(serverLeads);
        setMessage("Connected to backend! Data synchronized.");
      } else {
        setBackendStatus({
          connected: false,
          databaseConnected: false,
          databaseMode: "Frontend Demo Mode",
        });
        setError("Backend is starting up or unreachable. Please verify Spring Boot on port 8081.");
      }
    } catch {
      setBackendStatus({
        connected: false,
        databaseConnected: false,
        databaseMode: "Frontend Demo Mode",
      });
      setError("Backend is starting up or unreachable. Please verify Spring Boot on port 8081.");
    }
  };

  useEffect(() => {
    let isMounted = true;
    let retryTimer = null;

    const checkHealth = async () => {
      try {
        const health = await checkBackendHealth();
        if (!isMounted) return;

        if (health && health.status === "UP") {
          setBackendStatus({
            connected: true,
            databaseConnected: !!health.databaseConnected,
            databaseMode: health.databaseMode || "Spring Boot Active",
          });

          const [serverCustomers, serverLeads] = await Promise.all([
            fetchCustomers().catch(() => null),
            fetchLeads().catch(() => null),
          ]);
          if (!isMounted) return;
          if (serverCustomers && serverCustomers.length > 0) setCustomers(serverCustomers);
          if (serverLeads && serverLeads.length > 0) setLeads(serverLeads);
        } else {
          setBackendStatus({
            connected: false,
            databaseConnected: false,
            databaseMode: "Frontend Demo Mode",
          });
          // Auto-retry in 3 seconds to connect as soon as Spring Boot finishes starting
          retryTimer = setTimeout(checkHealth, 3000);
        }
      } catch {
        if (isMounted) {
          setBackendStatus({
            connected: false,
            databaseConnected: false,
            databaseMode: "Frontend Demo Mode",
          });
          retryTimer = setTimeout(checkHealth, 3000);
        }
      }
    };

    checkHealth();

    return () => {
      isMounted = false;
      if (retryTimer) clearTimeout(retryTimer);
    };
  }, []);

  // Dashboard statistics
  const convertedLeads = leads.filter(
    (lead) => lead.status === "Converted"
  ).length;

  const conversionRate =
    leads.length === 0
      ? 0
      : Math.round((convertedLeads / leads.length) * 100);

  const totalValue = leads.reduce(
    (sum, lead) => sum + (Number(lead.value) || 0),
    0
  );

  // Search customers
  const filteredCustomers = customers.filter((customer) =>
    `${customer.name} ${customer.email} ${customer.company}`
      .toLowerCase()
      .includes(search.toLowerCase().trim())
  );

  // Find customer name for each lead
  function getCustomerName(customerId) {
    const customer = customers.find(
      (item) => String(item.id) === String(customerId)
    );

    return customer ? customer.name : "Unknown customer";
  }

  // Show feedback messages
  function clearFeedback() {
    setMessage("");
    setError("");
  }

  // Navigate between pages
  function navigate(tab) {
    setActiveTab(tab);
    clearFeedback();
    setSearch("");
  }

  // Add a customer
  async function handleAddCustomer(event) {
    event.preventDefault();
    clearFeedback();

    const name = customerForm.name.trim();
    const email = customerForm.email.trim().toLowerCase();
    const company = customerForm.company.trim();
    const phone = customerForm.phone.trim();

    if (!name || !email || !company) {
      setError("Please complete all required customer fields.");
      return;
    }

    const validEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!validEmail.test(email)) {
      setError("Please enter a valid email address.");
      return;
    }

    const duplicate = customers.some(
      (customer) => customer.email.toLowerCase() === email
    );

    if (duplicate) {
      setError("A customer with this email already exists.");
      return;
    }

    const customerPayload = { name, email, company, phone };

    if (backendStatus.connected) {
      try {
        const savedCustomer = await createCustomerApi(customerPayload);
        setCustomers((prev) => [...prev, savedCustomer]);
        setMessage("Customer added successfully to backend!");
      } catch (err) {
        setError(err.message || "Failed to save customer to backend.");
        return;
      }
    } else {
      const newCustomer = {
        id: String(Date.now()),
        ...customerPayload,
      };
      setCustomers((previous) => [...previous, newCustomer]);
      setMessage("Customer added successfully (local).");
    }

    setCustomerForm({
      name: "",
      email: "",
      company: "",
      phone: "",
    });
  }

  // Delete a customer and their associated leads
  async function handleDeleteCustomer(customerId) {
    const confirmed = window.confirm(
      "Delete this customer and all their leads?"
    );

    if (!confirmed) return;

    if (backendStatus.connected) {
      try {
        await deleteCustomerApi(customerId);
        setCustomers((prev) => prev.filter((c) => String(c.id) !== String(customerId)));
        setLeads((prev) => prev.filter((l) => String(l.customerId) !== String(customerId)));
        clearFeedback();
        setMessage("Customer and associated leads deleted from backend.");
        return;
      } catch (err) {
        setError(err.message || "Failed to delete customer from backend.");
        return;
      }
    }

    setCustomers((previous) =>
      previous.filter((customer) => String(customer.id) !== String(customerId))
    );

    setLeads((previous) =>
      previous.filter((lead) => String(lead.customerId) !== String(customerId))
    );

    clearFeedback();
    setMessage("Customer and associated leads deleted.");
  }

  // Add a lead
  async function handleAddLead(event) {
    event.preventDefault();
    clearFeedback();

    if (
      !leadForm.customerId ||
      !leadForm.title.trim() ||
      leadForm.value === ""
    ) {
      setError("Please complete all required lead fields.");
      return;
    }

    const value = Number(leadForm.value);

    if (!Number.isFinite(value) || value < 0) {
      setError("Lead value must be zero or greater.");
      return;
    }

    const leadPayload = {
      customerId: String(leadForm.customerId),
      title: leadForm.title.trim(),
      value,
      status: "New",
    };

    if (backendStatus.connected) {
      try {
        const savedLead = await createLeadApi(leadPayload);
        setLeads((prev) => [...prev, savedLead]);
        setMessage("Lead created successfully in backend!");
      } catch (err) {
        setError(err.message || "Failed to create lead in backend.");
        return;
      }
    } else {
      const newLead = {
        id: String(Date.now()),
        ...leadPayload,
      };
      setLeads((previous) => [...previous, newLead]);
      setMessage("Lead created successfully (local).");
    }

    setLeadForm({
      customerId: "",
      title: "",
      value: "",
    });
  }

  // Update lead status
  async function handleStatusChange(leadId, status) {
    if (backendStatus.connected) {
      try {
        const updated = await updateLeadStatusApi(leadId, status);
        setLeads((prev) =>
          prev.map((lead) => (String(lead.id) === String(leadId) ? updated : lead))
        );
        clearFeedback();
        setMessage("Lead status updated in backend.");
        return;
      } catch (err) {
        setError(err.message || "Failed to update lead status in backend.");
        return;
      }
    }

    setLeads((previous) =>
      previous.map((lead) =>
        String(lead.id) === String(leadId) ? { ...lead, status } : lead
      )
    );

    clearFeedback();
    setMessage("Lead status updated successfully.");
  }

  // Rule-based AI recommendation
  function getInsight(lead) {
    if (lead.status === "New") {
      return {
        title: "Contact this lead",
        description:
          "Reach out soon, introduce your business, and understand the customer's needs.",
        action: "Recommended: First contact",
      };
    }

    if (lead.status === "Contacted") {
      return {
        title: "Follow up with the customer",
        description:
          "Ask whether the customer has questions and discuss the next steps.",
        action: "Recommended: Follow-up",
      };
    }

    return {
      title: "Maintain the relationship",
      description:
        "Thank the customer, confirm the next steps, and look for opportunities to provide continued value.",
      action: "Recommended: Customer retention",
    };
  }

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">C</div>
          <div>
            <h2>Clario CRM</h2>
            <p>Customer management</p>
          </div>
        </div>

        <p className="nav-heading">WORKSPACE</p>

        <nav className="navigation">
          <button
            className={activeTab === "Dashboard" ? "nav-link active" : "nav-link"}
            onClick={() => navigate("Dashboard")}
          >
            <span>▦</span> Dashboard
          </button>

          <button
            className={activeTab === "Customers" ? "nav-link active" : "nav-link"}
            onClick={() => navigate("Customers")}
          >
            <span>♙</span> Customers
          </button>

          <button
            className={activeTab === "Leads" ? "nav-link active" : "nav-link"}
            onClick={() => navigate("Leads")}
          >
            <span>◎</span> Leads
          </button>
        </nav>

        <div className="sidebar-bottom">
          <div className="help-card">
            <span>✦ AI ASSISTANT</span>
            <p>Get helpful recommendations for your sales leads.</p>
          </div>

          <div className="profile">
            <div className="avatar">CA</div>
            <div>
              <strong>CRM Admin</strong>
              <p>Demo workspace</p>
            </div>
          </div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <p className="breadcrumb">Workspace / {activeTab}</p>
            <h1>{activeTab}</h1>
          </div>

          <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
            <button
              type="button"
              className="text-button"
              style={{ fontSize: "0.85rem", cursor: "pointer" }}
              onClick={handleRefresh}
              title="Click to check backend connectivity and reload data"
            >
              ↻ Sync
            </button>
            <span
              className={`demo-badge ${backendStatus.connected ? "connected" : ""}`}
              title={backendStatus.databaseMode}
            >
              ● {backendStatus.connected
                  ? (backendStatus.databaseConnected ? "Spring Boot + Atlas" : "Spring Boot Active")
                  : "Frontend Demo Mode"}
            </span>
          </div>
        </header>

        <div className="page-content">
          {message && (
            <div className="alert success" role="status">
              ✓ {message}
            </div>
          )}

          {error && (
            <div className="alert error" role="alert">
              ⚠ {error}
            </div>
          )}

          {/* DASHBOARD PAGE */}
          {activeTab === "Dashboard" && (
            <section className="dashboard-page">
              <div className="welcome-banner">
                <div>
                  <span className="eyebrow">BUSINESS OVERVIEW</span>
                  <h2>Welcome to your CRM workspace!</h2>
                  <p>
                    Manage customers, track sales opportunities,
                    and discover your next best action.
                  </p>
                </div>
                <div className="welcome-icon">✦</div>
              </div>

              <div className="stats-grid">
                <article className="stat-card">
                  <div className="stat-icon purple">♙</div>
                  <p>Total Customers</p>
                  <h2>{customers.length}</h2>
                  <span>Registered customers</span>
                </article>

                <article className="stat-card">
                  <div className="stat-icon blue">◎</div>
                  <p>Total Leads</p>
                  <h2>{leads.length}</h2>
                  <span>All sales opportunities</span>
                </article>

                <article className="stat-card">
                  <div className="stat-icon green">✓</div>
                  <p>Converted Leads</p>
                  <h2>{convertedLeads}</h2>
                  <span>Successful conversions</span>
                </article>

                <article className="stat-card">
                  <div className="stat-icon orange">↗</div>
                  <p>Conversion Rate</p>
                  <h2>{conversionRate}%</h2>
                  <span>Converted leads / all leads</span>
                </article>
              </div>

              <div className="dashboard-grid">
                <section className="panel">
                  <div className="panel-heading">
                    <div>
                      <h2>Recent Leads</h2>
                      <p>Your latest sales opportunities</p>
                    </div>

                    <button
                      className="text-button"
                      onClick={() => navigate("Leads")}
                    >
                      View all →
                    </button>
                  </div>

                  {leads.length === 0 ? (
                    <p className="empty-state">
                      No leads yet. Create your first lead.
                    </p>
                  ) : (
                    <div className="table-wrapper">
                      <table>
                        <thead>
                          <tr>
                            <th>Opportunity</th>
                            <th>Customer</th>
                            <th>Status</th>
                          </tr>
                        </thead>

                        <tbody>
                          {[...leads].reverse().slice(0, 5).map((lead) => (
                            <tr key={lead.id}>
                              <td>
                                <strong>{lead.title}</strong>
                                <span className="cell-subtitle">
                                  ₹{Number(lead.value || 0).toLocaleString("en-IN")}
                                </span>
                              </td>

                              <td>{getCustomerName(lead.customerId)}</td>

                              <td>
                                <span className={`status-badge ${lead.status.toLowerCase()}`}>
                                  {lead.status}
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </section>

                <section className="panel">
                  <div className="panel-heading">
                    <div>
                      <span className="ai-label">✦ RULE-BASED AI</span>
                      <h2>AI Insights</h2>
                      <p>Suggested follow-up actions</p>
                    </div>
                  </div>

                  {leads.length === 0 ? (
                    <p className="empty-state">
                      Add a lead to receive a recommendation.
                    </p>
                  ) : (
                    <div className="insight-list">
                      {[...leads]
                        .reverse()
                        .slice(0, 3)
                        .map((lead) => {
                          const insight = getInsight(lead);

                          return (
                            <article className="insight-item" key={lead.id}>
                              <span className="insight-bullet">✦</span>
                              <div>
                                <h3>{lead.title}</h3>
                                <strong>{insight.title}</strong>
                                <p>{insight.description}</p>
                                <span className="recommendation">
                                  {insight.action}
                                </span>
                              </div>
                            </article>
                          );
                        })}
                    </div>
                  )}
                </section>
              </div>

              <section className="panel pipeline-summary">
                <div className="panel-heading">
                  <div>
                    <h2>Sales Pipeline Summary</h2>
                    <p>Distribution of your current leads</p>
                  </div>

                  <strong>
                    ₹{totalValue.toLocaleString("en-IN")}
                  </strong>
                </div>

                <div className="pipeline-grid">
                  {["New", "Contacted", "Converted"].map((status) => {
                    const count = leads.filter(
                      (lead) => lead.status === status
                    ).length;

                    return (
                      <div className="pipeline-item" key={status}>
                        <span className={`status-badge ${status.toLowerCase()}`}>
                          {status}
                        </span>
                        <h2>{count}</h2>
                        <p>Leads</p>
                      </div>
                    );
                  })}
                </div>
              </section>
            </section>
          )}

          {/* CUSTOMERS PAGE */}
          {activeTab === "Customers" && (
            <section className="customer-page">
              <section className="panel form-panel">
                <div className="panel-heading">
                  <div>
                    <h2>Add New Customer</h2>
                    <p>Enter the customer's contact details.</p>
                  </div>
                </div>

                <form className="customer-form" onSubmit={handleAddCustomer}>
                  <div className="form-field">
                    <label htmlFor="customer-name">Full Name *</label>
                    <input
                      id="customer-name"
                      value={customerForm.name}
                      onChange={(event) =>
                        setCustomerForm({
                          ...customerForm,
                          name: event.target.value,
                        })
                      }
                      placeholder="e.g. Priya Sharma"
                      required
                    />
                  </div>

                  <div className="form-field">
                    <label htmlFor="customer-email">Email Address *</label>
                    <input
                      id="customer-email"
                      type="email"
                      value={customerForm.email}
                      onChange={(event) =>
                        setCustomerForm({
                          ...customerForm,
                          email: event.target.value,
                        })
                      }
                      placeholder="name@example.com"
                      required
                    />
                  </div>

                  <div className="form-field">
                    <label htmlFor="customer-company">Company *</label>
                    <input
                      id="customer-company"
                      value={customerForm.company}
                      onChange={(event) =>
                        setCustomerForm({
                          ...customerForm,
                          company: event.target.value,
                        })
                      }
                      placeholder="Company name"
                      required
                    />
                  </div>

                  <div className="form-field">
                    <label htmlFor="customer-phone">Phone Number</label>
                    <input
                      id="customer-phone"
                      type="tel"
                      value={customerForm.phone}
                      onChange={(event) =>
                        setCustomerForm({
                          ...customerForm,
                          phone: event.target.value,
                        })
                      }
                      placeholder="Phone number"
                    />
                  </div>

                  <button className="primary-button" type="submit">
                    + Add Customer
                  </button>
                </form>
              </section>

              <section className="panel">
                <div className="panel-heading">
                  <div>
                    <h2>Customer Directory</h2>
                    <p>{customers.length} registered customers</p>
                  </div>
                </div>

                <div className="search-box">
                  <span>⌕</span>
                  <input
                    type="search"
                    placeholder="Search name, email or company..."
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                  />
                </div>

                {filteredCustomers.length === 0 ? (
                  <p className="empty-state">No matching customers found.</p>
                ) : (
                  <div className="customer-list">
                    {filteredCustomers.map((customer) => (
                      <article className="customer-row" key={customer.id}>
                        <div className="avatar customer-avatar">
                          {customer.name
                            .split(" ")
                            .map((part) => part[0])
                            .join("")
                            .slice(0, 2)
                            .toUpperCase()}
                        </div>

                        <div className="customer-info">
                          <strong>{customer.name}</strong>
                          <span>{customer.company}</span>
                          <span>{customer.email}</span>
                          {customer.phone && <span>{customer.phone}</span>}
                        </div>

                        <button
                          className="danger-button"
                          onClick={() => handleDeleteCustomer(customer.id)}
                        >
                          Delete
                        </button>
                      </article>
                    ))}
                  </div>
                )}
              </section>
            </section>
          )}

          {/* LEADS PAGE */}
          {activeTab === "Leads" && (
            <section className="leads-page">
              <section className="panel form-panel">
                <div className="panel-heading">
                  <div>
                    <h2>Create Sales Lead</h2>
                    <p>Connect an opportunity to an existing customer.</p>
                  </div>
                </div>

                {customers.length === 0 ? (
                  <p className="empty-state">
                    Add a customer before creating a lead.
                  </p>
                ) : (
                  <form className="lead-form" onSubmit={handleAddLead}>
                    <div className="form-field">
                      <label htmlFor="lead-customer">Customer *</label>
                      <select
                        id="lead-customer"
                        value={leadForm.customerId}
                        onChange={(event) =>
                          setLeadForm({
                            ...leadForm,
                            customerId: event.target.value,
                          })
                        }
                        required
                      >
                        <option value="">Select a customer</option>
                        {customers.map((customer) => (
                          <option key={customer.id} value={customer.id}>
                            {customer.name}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="form-field">
                      <label htmlFor="lead-title">Opportunity *</label>
                      <input
                        id="lead-title"
                        value={leadForm.title}
                        onChange={(event) =>
                          setLeadForm({
                            ...leadForm,
                            title: event.target.value,
                          })
                        }
                        placeholder="e.g. Website Development"
                        required
                      />
                    </div>

                    <div className="form-field">
                      <label htmlFor="lead-value">Estimated Value (₹) *</label>
                      <input
                        id="lead-value"
                        type="number"
                        min="0"
                        step="0.01"
                        value={leadForm.value}
                        onChange={(event) =>
                          setLeadForm({
                            ...leadForm,
                            value: event.target.value,
                          })
                        }
                        placeholder="25000"
                        required
                      />
                    </div>

                    <button className="primary-button" type="submit">
                      + Create Lead
                    </button>
                  </form>
                )}
              </section>

              <section className="panel">
                <div className="panel-heading">
                  <div>
                    <h2>Sales Pipeline</h2>
                    <p>Manage your opportunities and their status.</p>
                  </div>
                  <span className="count-badge">{leads.length} leads</span>
                </div>

                {leads.length === 0 ? (
                  <p className="empty-state">
                    No leads available. Create your first lead.
                  </p>
                ) : (
                  <div className="table-wrapper">
                    <table>
                      <thead>
                        <tr>
                          <th>Opportunity</th>
                          <th>Customer</th>
                          <th>Value</th>
                          <th>Status</th>
                          <th>AI Recommendation</th>
                        </tr>
                      </thead>

                      <tbody>
                        {leads.map((lead) => {
                          const insight = getInsight(lead);

                          return (
                            <tr key={lead.id}>
                              <td>
                                <strong>{lead.title}</strong>
                              </td>
                              <td>{getCustomerName(lead.customerId)}</td>
                              <td>
                                ₹{Number(lead.value || 0).toLocaleString("en-IN")}
                              </td>
                              <td>
                                <select
                                  className="status-select"
                                  aria-label={`Status for ${lead.title}`}
                                  value={lead.status}
                                  onChange={(event) =>
                                    handleStatusChange(
                                      lead.id,
                                      event.target.value
                                    )
                                  }
                                >
                                  <option value="New">New</option>
                                  <option value="Contacted">Contacted</option>
                                  <option value="Converted">Converted</option>
                                </select>
                              </td>
                              <td>
                                <span className="recommendation">
                                  {insight.title}
                                </span>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                )}
              </section>
            </section>
          )}

          <footer className="footer">
            <span>Clario CRM · AI CRM Software</span>
            <span>React + Vite · Frontend Demo</span>
          </footer>
        </div>
      </main>
    </div>
  );
}

export default App;