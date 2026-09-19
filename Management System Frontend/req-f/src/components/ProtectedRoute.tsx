import { Navigate } from "react-router-dom";

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles: string[];
}

function ProtectedRoute({
  children,
  allowedRoles,
}: ProtectedRouteProps) {

  const token = localStorage.getItem("token");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  let role = "";

  try {

    const payload = JSON.parse(
      atob(token.split(".")[1])
    );

    role = payload.role;

  } catch (error) {

    console.error("Invalid token");

    localStorage.removeItem("token");

    return <Navigate to="/login" replace />;
  }

  if (!allowedRoles.includes(role)) {

    return (
      <Navigate
        to="/dashboard"
        replace
      />
    );
  }

  return <>{children}</>;
}

export default ProtectedRoute;