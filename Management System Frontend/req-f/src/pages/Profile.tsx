import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import {
  User as UserIcon,
  Lock,
  FileText,
  Upload,
  Download,
} from "lucide-react";

import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";

import api from "../api/axios";

type CurrentUser = {
  name: string;
  email: string;
  role: string;
};

function Profile() {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);

  const [name, setName] = useState("");
  const [savingProfile, setSavingProfile] = useState(false);

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [savingPassword, setSavingPassword] = useState(false);

  const [documents, setDocuments] = useState<any[]>([]);
  const [uploadingCV, setUploadingCV] = useState(false);
  const [uploadingAttachment, setUploadingAttachment] = useState(false);

  const fetchProfile = async () => {
    try {
      const res = await api.get("/me");
      setUser(res.data);
      setName(res.data.name || "");
    } catch (error) {
      console.log(error);
      toast.error("Failed to load profile");
    } finally {
      setLoading(false);
    }
  };

  const fetchDocuments = async () => {
    try {
      const res = await api.get("/profile/documents");
      setDocuments(res.data);
    } catch (error) {
      console.log(error);
    }
  };

  useEffect(() => {
    fetchProfile();
    fetchDocuments();
  }, []);

  // ===============================
  // رفع الـ CV — منطق كامل هالمرة
  // ===============================
  const handleUploadCV = async (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = e.target.files?.[0];

    if (!file) {
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      setUploadingCV(true);

      await api.post("/profile/documents/cv", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      toast.success("CV uploaded successfully");

      await fetchDocuments();
    } catch (error: any) {
      toast.error(
        error.response?.data?.message || "Failed to upload CV"
      );
    } finally {
      setUploadingCV(false);
      e.target.value = "";
    }
  };

  // ===============================
  // رفع المرفق — الشرط اتصلح، المنطق صار برا الـ if
  // ===============================
  const handleUploadAttachment = async (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = e.target.files?.[0];

    if (!file) {
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      setUploadingAttachment(true);

      await api.post("/profile/documents/attachment", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      toast.success("Attachment uploaded successfully");

      await fetchDocuments();
    } catch (error: any) {
      toast.error(
        error.response?.data?.message ||
          "Failed to upload attachment"
      );
    } finally {
      setUploadingAttachment(false);
      e.target.value = "";
    }
  };

  const handleSaveProfile = async () => {
    if (!name.trim()) {
      toast.error("Name cannot be empty");
      return;
    }

    setSavingProfile(true);

    try {
      const res = await api.put("/me", { name });
      setUser(res.data);
      toast.success("Profile updated");
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to update profile";
      toast.error(message);
    } finally {
      setSavingProfile(false);
    }
  };

  const handleChangePassword = async () => {
    if (!currentPassword || !newPassword || !confirmPassword) {
      toast.error("Please fill in all password fields");
      return;
    }

    if (newPassword.length < 6) {
      toast.error("New password must be at least 6 characters");
      return;
    }

    if (newPassword !== confirmPassword) {
      toast.error("New passwords don't match");
      return;
    }

    setSavingPassword(true);

    try {
      await api.put("/me/password", {
        currentPassword,
        newPassword,
      });

      toast.success("Password updated");

      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (error: any) {
      const message =
        error.response?.data?.message || "Failed to update password";
      toast.error(message);
    } finally {
      setSavingPassword(false);
    }
  };

  if (loading) {
    return (
      <div
        className="
          min-h-screen
          bg-[#020617]
          flex
          items-center
          justify-center
        "
      >
        <div
          className="
            w-16
            h-16
            border-4
            border-blue-500
            border-t-transparent
            rounded-full
            animate-spin
          "
        />
      </div>
    );
  }

  const initial = user?.name ? user.name.charAt(0).toUpperCase() : "";

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

        <div className="p-8">
          <div className="mb-10">
            <h1 className="text-5xl font-black tracking-tight">
              My Profile
            </h1>
            <p className="text-gray-400 mt-2 text-lg">
              Manage your account information
            </p>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 max-w-4xl">
            {/* PROFILE INFO */}
            <div
              className="
                bg-white/5
                border
                border-white/10
                backdrop-blur-2xl
                rounded-[32px]
                p-8
              "
            >
              <div className="flex items-center gap-4 mb-8">
                <div
                  className="
                    w-16
                    h-16
                    rounded-2xl
                    bg-gradient-to-br
                    from-blue-500
                    to-cyan-400
                    flex
                    items-center
                    justify-center
                    font-black
                    text-2xl
                    shadow-lg
                  "
                >
                  {initial}
                </div>

                <div>
                  <p className="font-bold text-lg">{user?.name}</p>
                  <span
                    className={`
                      inline-block
                      mt-1
                      px-3
                      py-1
                      rounded-full
                      text-xs
                      font-bold
                      ${
                        user?.role === "ADMIN"
                          ? "bg-purple-500/20 text-purple-400"
                          : "bg-blue-500/20 text-blue-400"
                      }
                    `}
                  >
                    {user?.role}
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-2 mb-5">
                <UserIcon size={18} className="text-blue-400" />
                <h2 className="text-lg font-bold">Profile Information</h2>
              </div>

              <div className="flex flex-col gap-4">
                <div className="flex flex-col gap-2">
                  <label className="text-gray-400 text-sm font-semibold">
                    Full Name
                  </label>
                  <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="
                      bg-white/5
                      border
                      border-white/10
                      rounded-2xl
                      px-5
                      py-3
                      text-white
                      outline-none
                      focus:border-blue-400/50
                      transition
                    "
                  />
                </div>

                <div className="flex flex-col gap-2">
                  <label className="text-gray-400 text-sm font-semibold">
                    Email
                  </label>
                  <input
                    type="text"
                    value={user?.email || ""}
                    disabled
                    className="
                      bg-white/5
                      border
                      border-white/10
                      rounded-2xl
                      px-5
                      py-3
                      text-gray-500
                      outline-none
                      cursor-not-allowed
                    "
                  />
                  <p className="text-xs text-gray-500">
                    Email can't be changed here since it's tied to your
                    login.
                  </p>
                </div>

                <button
                  onClick={handleSaveProfile}
                  disabled={savingProfile}
                  className="
                    bg-gradient-to-r
                    from-blue-500
                    to-cyan-500
                    hover:scale-[1.02]
                    transition-all
                    duration-300
                    px-6
                    py-3
                    rounded-2xl
                    font-semibold
                    shadow-lg
                    shadow-blue-500/20
                    disabled:opacity-50
                    disabled:cursor-not-allowed
                    disabled:hover:scale-100
                  "
                >
                  {savingProfile ? "Saving..." : "Save Changes"}
                </button>
              </div>
            </div>

            {/* CHANGE PASSWORD */}
            <div
              className="
                bg-white/5
                border
                border-white/10
                backdrop-blur-2xl
                rounded-[32px]
                p-8
              "
            >
              <div className="flex items-center gap-2 mb-5">
                <Lock size={18} className="text-blue-400" />
                <h2 className="text-lg font-bold">Change Password</h2>
              </div>

              <div className="flex flex-col gap-4">
                <div className="flex flex-col gap-2">
                  <label className="text-gray-400 text-sm font-semibold">
                    Current Password
                  </label>
                  <input
                    type="password"
                    value={currentPassword}
                    onChange={(e) =>
                      setCurrentPassword(e.target.value)
                    }
                    className="
                      bg-white/5
                      border
                      border-white/10
                      rounded-2xl
                      px-5
                      py-3
                      text-white
                      outline-none
                      focus:border-blue-400/50
                      transition
                    "
                  />
                </div>

                <div className="flex flex-col gap-2">
                  <label className="text-gray-400 text-sm font-semibold">
                    New Password
                  </label>
                  <input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    className="
                      bg-white/5
                      border
                      border-white/10
                      rounded-2xl
                      px-5
                      py-3
                      text-white
                      outline-none
                      focus:border-blue-400/50
                      transition
                    "
                  />
                </div>

                <div className="flex flex-col gap-2">
                  <label className="text-gray-400 text-sm font-semibold">
                    Confirm New Password
                  </label>
                  <input
                    type="password"
                    value={confirmPassword}
                    onChange={(e) =>
                      setConfirmPassword(e.target.value)
                    }
                    className="
                      bg-white/5
                      border
                      border-white/10
                      rounded-2xl
                      px-5
                      py-3
                      text-white
                      outline-none
                      focus:border-blue-400/50
                      transition
                    "
                  />
                </div>

                <button
                  onClick={handleChangePassword}
                  disabled={savingPassword}
                  className="
                    bg-white/10
                    hover:bg-white/20
                    border
                    border-white/10
                    transition-all
                    duration-300
                    px-6
                    py-3
                    rounded-2xl
                    font-semibold
                    disabled:opacity-50
                    disabled:cursor-not-allowed
                  "
                >
                  {savingPassword ? "Updating..." : "Update Password"}
                </button>
              </div>
            </div>

            {/* DOCUMENTS — lg:col-span-2 عشان تاخذ العرض كامل */}
            <div
              className="
                lg:col-span-2
                bg-white/5
                border
                border-white/10
                backdrop-blur-2xl
                rounded-[32px]
                p-8
              "
            >
              <div className="flex items-center gap-2 mb-6">
                <FileText size={18} className="text-purple-400" />
                <h2 className="text-lg font-bold">Documents</h2>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* CV */}
                <div
                  className="
                    p-5
                    rounded-2xl
                    bg-purple-500/5
                    border
                    border-purple-500/20
                  "
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-semibold">CV / Resume</p>
                      <p className="text-sm text-gray-500 mt-1">
                        PDF, DOC or DOCX
                      </p>
                    </div>

                    <FileText size={24} className="text-purple-400" />
                  </div>

                  {documents
                    .filter((doc) => doc.type === "CV")
                    .map((doc) => (
                      <div
                        key={doc.id}
                        className="
                          mt-4
                          p-3
                          rounded-xl
                          bg-white/5
                          border
                          border-white/10
                        "
                      >
                        <p className="text-sm truncate">
                          {doc.originalName}
                        </p>

                        <a
                          href={`http://localhost:8080/profile/documents/${doc.id}/download`}
                          target="_blank"
                          rel="noreferrer"
                          className="
                            mt-2
                            inline-flex
                            items-center
                            gap-2
                            text-blue-400
                            hover:text-blue-300
                            text-sm
                          "
                        >
                          <Download size={16} />
                          Download
                        </a>
                      </div>
                    ))}

                  <label
                    className="
                      mt-4
                      inline-flex
                      items-center
                      gap-2
                      px-4
                      py-2.5
                      rounded-2xl
                      bg-purple-600
                      hover:bg-purple-700
                      cursor-pointer
                      font-semibold
                      transition
                    "
                  >
                    <Upload size={17} />
                    {uploadingCV ? "Uploading..." : "Upload CV"}

                    <input
                      type="file"
                      accept=".pdf,.doc,.docx"
                      className="hidden"
                      onChange={handleUploadCV}
                      disabled={uploadingCV}
                    />
                  </label>
                </div>

                {/* ATTACHMENTS */}
                <div
                  className="
                    p-5
                    rounded-2xl
                    bg-blue-500/5
                    border
                    border-blue-500/20
                  "
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-semibold">Attachments</p>
                      <p className="text-sm text-gray-500 mt-1">
                        Supporting documents
                      </p>
                    </div>

                    <Upload size={24} className="text-blue-400" />
                  </div>

                  <div className="mt-4 space-y-2">
                    {documents
                      .filter((doc) => doc.type === "ATTACHMENT")
                      .map((doc) => (
                        <div
                          key={doc.id}
                          className="
                            flex
                            items-center
                            justify-between
                            gap-3
                            p-3
                            rounded-xl
                            bg-white/5
                            border
                            border-white/10
                          "
                        >
                          <span className="text-sm truncate">
                            {doc.originalName}
                          </span>

                          <a
                            href={`http://localhost:8080/profile/documents/${doc.id}/download`}
                            target="_blank"
                            rel="noreferrer"
                            className="text-blue-400"
                          >
                            <Download size={17} />
                          </a>
                        </div>
                      ))}
                  </div>

                  <label
                    className="
                      mt-4
                      inline-flex
                      items-center
                      gap-2
                      px-4
                      py-2.5
                      rounded-2xl
                      bg-blue-600
                      hover:bg-blue-700
                      cursor-pointer
                      font-semibold
                      transition
                    "
                  >
                    <Upload size={17} />
                    {uploadingAttachment
                      ? "Uploading..."
                      : "Upload Attachment"}

                    <input
                      type="file"
                      accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
                      className="hidden"
                      onChange={handleUploadAttachment}
                      disabled={uploadingAttachment}
                    />
                  </label>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Profile;