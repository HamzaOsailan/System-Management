
import { Link, useLocation } from "react-router-dom";
import {
  LayoutDashboard,
  FileText,
  PlusCircle,
  ShieldCheck,
  Users,
  UserCircle,
  Fingerprint,
} from "lucide-react";

function Sidebar() {
  const location = useLocation();
  const token = localStorage.getItem("token");

  let role = "USER";

  try {
    if (token) {
      const payload = JSON.parse(atob(token.split(".")[1]));

      role = payload.role || "USER";

      // In case the JWT returns "ROLE_ADMIN"
      if (role.startsWith("ROLE_")) {
        role = role.replace("ROLE_", "");
      }
    }
  } catch (error) {
    console.log("Could not read JWT role");
  }

  // =====================================================
  // USER MENU
  // =====================================================

  const userMenuItems = [
    {
      name: "Dashboard",
      path: "/dashboard",
      icon: <LayoutDashboard size={20} />,
    },
    {
      name: "My Requests",
      path: "/my-requests",
      icon: <FileText size={20} />,
    },
    {
      name: "Create Request",
      path: "/create",
      icon: <PlusCircle size={20} />,
    },
    {
      name: "Attendance",
      path: "/attendance",
      icon: <Fingerprint size={20} />,
    },
    {
      name: "Profile",
      path: "/profile",
      icon: <UserCircle size={20} />,
    },
  ];

  // =====================================================
  // ADMIN MENU
  // =====================================================

  const adminMenuItems = [
    {
      name: "Dashboard",
      path: "/dashboard",
      icon: <LayoutDashboard size={20} />,
    },
    {
      name: "Admin Requests",
      path: "/admin-requests",
      icon: <ShieldCheck size={20} />,
    },
    {
      name: "Users",
      path: "/users",
      icon: <Users size={20} />,
    },
    {
      name: "Attendance",
      path: "/attendance",
      icon: <Fingerprint size={20} />,
    },
    {
      name: "Profile",
      path: "/profile",
      icon: <UserCircle size={20} />,
    },
  ];

  // =====================================================
  // MANAGER MENU
  // =====================================================

  const managerMenuItems = [
    {
      name: "Dashboard",
      path: "/dashboard",
      icon: <LayoutDashboard size={20} />,
    },
    {
      name: "Manager Approvals",
      path: "/manager-requests",
      icon: <ShieldCheck size={20} />,
    },
    {
      name: "Attendance",
      path: "/attendance",
      icon: <Fingerprint size={20} />,
    },
    {
      name: "Profile",
      path: "/profile",
      icon: <UserCircle size={20} />,
    },
  ];

  // =====================================================
  // SELECT MENU
  // =====================================================

  let menuItems = userMenuItems;

  if (role === "ADMIN") {
    menuItems = adminMenuItems;
  } else if (role === "MANAGER") {
    menuItems = managerMenuItems;
  }

  // =====================================================
  // RENDER
  // =====================================================

  return (
    <aside
      className="
        w-72
        min-h-screen
        bg-[#020617]
        border-r
        border-white/10
        p-6
        flex
        flex-col
      "
    >
      {/* =================================================
          LOGO
      ================================================== */}

      <div className="mb-10">
        <h1 className="text-3xl font-black text-white">
          Management System
        </h1>

        <p className="text-gray-500 text-sm mt-1">
          Management System
        </p>
      </div>

      {/* =================================================
          CURRENT ROLE
      ================================================== */}

      <div
        className="
          mb-6
          px-4
          py-3
          rounded-2xl
          bg-white/5
          border
          border-white/10
        "
      >
        <p className="text-xs text-gray-500">
          Current Role
        </p>

        <p
          className={`
            mt-1
            font-bold
            ${
              role === "ADMIN"
                ? "text-blue-400"
                : role === "MANAGER"
                ? "text-purple-400"
                : "text-green-400"
            }
          `}
        >
          {role}
        </p>
      </div>

      {/* =================================================
          NAVIGATION
      ================================================== */}

      <nav className="flex flex-col gap-3">
        {menuItems.map((item) => {
          const isActive =
            location.pathname === item.path;

          return (
            <Link
              key={item.path}
              to={item.path}
              className={`
                flex
                items-center
                gap-4
                px-5
                py-4
                rounded-2xl
                transition-all
                duration-300
                ${
                  isActive
                    ? "bg-blue-500 text-white shadow-lg shadow-blue-500/20"
                    : "text-gray-400 hover:text-white hover:bg-white/5"
                }
              `}
            >
              {item.icon}

              <span className="font-semibold">
                {item.name}
              </span>
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}

export default Sidebar;

