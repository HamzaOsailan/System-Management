
import { BrowserRouter, Routes, Route } from "react-router-dom";

import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import MyRequests from "./pages/MyRequests";
import CreateRequest from "./pages/CreateRequest";
import AdminRequests from "./pages/AdminRequests";
import Users from "./pages/Users";
import Profile from "./pages/Profile";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";
import AdminSLA from "./pages/AdminSLA";
import AIRequestAnalysis from "./pages/AIRequestAnalysis";
import AdminAIAnalysis from "./pages/AdminAIAnalysis";
import Attendance from "./pages/Attendance";
import ProtectedRoute from "./components/ProtectedRoute";
import ManagerRequests from "./pages/ManagerRequests";
import RequestWorkflow from "./pages/RequestWorkflow";

function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* PUBLIC */}
        <Route path="/login" element={<Login />} />

        <Route
          path="/forgot-password"
          element={<ForgotPassword />}
        />

        <Route
          path="/reset-password"
          element={<ResetPassword />}
        />

        {/* USER + ADMIN */}
       
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute
              allowedRoles={["USER", "ADMIN", "MANAGER"]}
            >
              <Dashboard />
            </ProtectedRoute>
          }
        />
        


        <Route
          path="/my-requests"
          element={
            <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
              <MyRequests />
            </ProtectedRoute>
          }
        />

        <Route
          path="/requests/my"
          element={
            <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
              <MyRequests />
            </ProtectedRoute>
          }
        />

        <Route
          path="/create"
          element={
            <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
              <CreateRequest />
            </ProtectedRoute>
          }
        />

       <Route
  path="/profile"
  element={
    <ProtectedRoute
      allowedRoles={["USER", "ADMIN", "MANAGER"]}
    >
      <Profile />
    </ProtectedRoute>
  }
/>
        {/* ADMIN */}
        <Route
          path="/admin-requests"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminRequests />
            </ProtectedRoute>
          }
        />

        <Route
          path="/users"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <Users />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/sla"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminSLA />
            </ProtectedRoute>
          }
        />

        {/* USER AI ANALYSIS */}
        <Route
          path="/requests/:id/ai-analysis"
          element={
            <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
              <AIRequestAnalysis />
            </ProtectedRoute>
          }
        />

        {/* ADMIN AI ANALYSIS */}
        <Route
          path="/requests/:id/ai-analysis/admin"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminAIAnalysis />
            </ProtectedRoute>
          }
        />


        <Route
          path="/attendance"
          element={
            <ProtectedRoute allowedRoles={["USER", "ADMIN"]}>
              <Attendance />
            </ProtectedRoute>
          }
        />


        <Route
          path="/manager-requests"
          element={
            <ProtectedRoute allowedRoles={["MANAGER"]}>
              <ManagerRequests />
            </ProtectedRoute>
          }
        />

        <Route
          path="/requests/:id/workflow"
          element={
            <ProtectedRoute allowedRoles={["USER", "ADMIN", "MANAGER"]}>
              <RequestWorkflow />
            </ProtectedRoute>
          }
        />



      </Routes>

    </BrowserRouter>
  );
}

export default App;

