import { useEffect, useState } from "react";
import { Search, Users as UsersIcon, Trash2 } from "lucide-react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import api from "../api/axios";
import toast from "react-hot-toast";

type User = {
  id: number;
  name: string;
  email: string;
  role: "USER" | "ADMIN";
};

type CurrentUser = {
  name: string;
  email: string;
  role: string;
};

function Users() {
  const [users, setUsers] = useState<User[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [actingId, setActingId] = useState<number | null>(null);

  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(
    null
  );

  const fetchCurrentUser = async () => {
    try {
      const res = await api.get("/me");
      setCurrentUser(res.data);
    } catch (error) {
      console.log("Failed to fetch current user:", error);
    }
  };

  const fetchUsers = async () => {
    try {
      setLoading(true);

      const res = await api.get("/users");

      setUsers(res.data);
    } catch (error: any) {
      console.log("Users Error =", error);
      console.log("Response =", error.response);

      if (error.response?.status === 403) {
        toast.error("Access denied. Admin only.");
      } else {
        toast.error("Failed to fetch users");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCurrentUser();
    fetchUsers();
  }, []);

  const filteredUsers = users.filter((user) => {
    const value = search.toLowerCase();

    return (
      user.name.toLowerCase().includes(value) ||
      user.email.toLowerCase().includes(value) ||
      user.role.toLowerCase().includes(value)
    );
  });


  const handleRoleChange = async (
    userId: number,
    newRole: "USER" | "ADMIN"
  ) => {
    setActingId(userId);

    try {
      const res = await api.put(`/users/${userId}/role`, {
        role: newRole,
      });

      setUsers((prev) =>
        prev.map((u) =>
          u.id === userId ? { ...u, role: res.data.role } : u
        )
      );

      toast.success("Role updated");
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to update role";
      toast.error(message);
    } finally {
      setActingId(null);
    }
  };


  const handleDelete = async (userId: number, userName: string) => {
    const confirmed = window.confirm(
      `Delete ${userName}? This cannot be undone.`
    );

    if (!confirmed) return;

    setActingId(userId);

    try {
      await api.delete(`/users/${userId}`);

      setUsers((prev) => prev.filter((u) => u.id !== userId));

      toast.success("User deleted");
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to delete user";
      toast.error(message);
    } finally {
      setActingId(null);
    }
  };

  return (
    <div
      className="
        flex
        bg-gradient-to-br
        from-[#020617]
        via-[#07122b]
        to-[#020617]
        min-h-screen
        text-white
      "
    >
      <Sidebar />

      <div className="flex-1">
        <Navbar />

        <main className="p-8">
          <div className="flex items-center justify-between mb-8">
            <div>
              <h1 className="text-4xl font-black">Users Management</h1>

              <p className="text-gray-400 mt-2">
                Manage system users and their roles
              </p>
            </div>

            <div className="flex items-center gap-3 bg-white/5 border border-white/10 px-5 py-4 rounded-2xl">
              <UsersIcon size={22} className="text-blue-400" />

              <div>
                <p className="text-xs text-gray-400">Total Users</p>

                <p className="text-xl font-bold">{users.length}</p>
              </div>
            </div>
          </div>

          <div className="relative mb-6">
            <Search
              size={20}
              className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400"
            />

            <input
              type="text"
              placeholder="Search by name, email or role..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full bg-white/5 border border-white/10 rounded-2xl py-4 pl-12 pr-5 text-white outline-none focus:border-blue-500 transition"
            />
          </div>

          <div className="bg-white/5 border border-white/10 rounded-[28px] overflow-hidden backdrop-blur-xl">
            {loading ? (
              <div className="p-10 text-center text-gray-400">
                Loading users...
              </div>
            ) : filteredUsers.length === 0 ? (
              <div className="p-10 text-center text-gray-400">
                No users found.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-white/5 border-b border-white/10">
                    <tr>
                      <th className="text-left px-6 py-5 text-gray-400 font-semibold">
                        ID
                      </th>

                      <th className="text-left px-6 py-5 text-gray-400 font-semibold">
                        Name
                      </th>

                      <th className="text-left px-6 py-5 text-gray-400 font-semibold">
                        Email
                      </th>

                      <th className="text-left px-6 py-5 text-gray-400 font-semibold">
                        Role
                      </th>

                      <th className="text-left px-6 py-5 text-gray-400 font-semibold">
                        Actions
                      </th>
                    </tr>
                  </thead>

                  <tbody>
                    {filteredUsers.map((user) => {
                      const isSelf =
                        currentUser?.email === user.email;

                      const isActing = actingId === user.id;

                      return (
                        <tr
                          key={user.id}
                          className="border-b border-white/5 hover:bg-white/5 transition"
                        >
                          <td className="px-6 py-5 text-gray-400">
                            #{user.id}
                          </td>

                          <td className="px-6 py-5">
                            <div className="flex items-center gap-3">
                              <div className="w-10 h-10 rounded-xl bg-blue-500/20 flex items-center justify-center text-blue-400 font-bold">
                                {user.name.charAt(0).toUpperCase()}
                              </div>

                              <span className="font-semibold">
                                {user.name}
                                {isSelf && (
                                  <span className="text-gray-500 font-normal">
                                    {" "}
                                    (you)
                                  </span>
                                )}
                              </span>
                            </div>
                          </td>

                          <td className="px-6 py-5 text-gray-300">
                            {user.email}
                          </td>

                          <td className="px-6 py-5">
                            {isSelf ? (
                              <span
                                className={`px-4 py-2 rounded-xl text-sm font-bold ${
                                  user.role === "ADMIN"
                                    ? "bg-purple-500/20 text-purple-400"
                                    : "bg-blue-500/20 text-blue-400"
                                }`}
                              >
                                {user.role}
                              </span>
                            ) : (
                              <select
                                value={user.role}
                                disabled={isActing}
                                onChange={(e) =>
                                  handleRoleChange(
                                    user.id,
                                    e.target.value as "USER" | "ADMIN"
                                  )
                                }
                                className={`px-4 py-2 rounded-xl text-sm font-bold outline-none cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed ${
                                  user.role === "ADMIN"
                                    ? "bg-purple-500/20 text-purple-400"
                                    : "bg-blue-500/20 text-blue-400"
                                }`}
                              >
                                <option
                                  value="USER"
                                  className="bg-[#0f172a] text-white"
                                >
                                  USER
                                </option>
                                <option
                                  value="ADMIN"
                                  className="bg-[#0f172a] text-white"
                                >
                                  ADMIN
                                </option>
                                 <option
                                  value="MANAGER"
                                  className="bg-[#0f172a] text-white"
                                >
                                  MANAGER
                                </option>
                              </select>
                            )}
                          </td>

                          <td className="px-6 py-5">
                            {!isSelf && (
                              <button
                                onClick={() =>
                                  handleDelete(user.id, user.name)
                                }
                                disabled={isActing}
                                className="
                                  p-2
                                  rounded-xl
                                  bg-red-500/10
                                  text-red-400
                                  hover:bg-red-500/20
                                  transition
                                  disabled:opacity-50
                                  disabled:cursor-not-allowed
                                "
                                title="Delete user"
                              >
                                <Trash2 size={18} />
                              </button>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </main>
      </div>
    </div>
  );
}

export default Users;