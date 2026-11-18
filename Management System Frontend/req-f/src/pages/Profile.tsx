import {
  useEffect,
  useState,
  type ChangeEvent,
} from "react";
import { useTranslation } from "react-i18next";
import axios from "axios";
import {
  AlertCircle,
  Loader2,
  RefreshCw,
  ShieldCheck,
  Sparkles,
  UserRound,
} from "lucide-react";
import { motion } from "framer-motion";


import ProfileHeader from "../components/profile/ProfileHeader";
import ProfileTabs from "../components/profile/ProfileTabs";
import ProfileOverview from "../components/profile/ProfileOverview";
import ProfilePersonal from "../components/profile/ProfilePersonal";
import ProfileEmployment from "../components/profile/ProfileEmployment";
import ProfileDocuments from "../components/profile/ProfileDocuments";
import ProfileSecurity from "../components/profile/ProfileSecurity";

import EditProfileModal, {
  type ProfileFormData,
} from "../components/profile/EditProfileModal";

import ChangePasswordModal from "../components/profile/ChangePasswordModal";

import type {
  DocumentItem,
  EmployeeProfile,
  TabType,
} from "../types/profile";

const API_URL =
  import.meta.env.VITE_API_URL ||
  "http://localhost:8080";

interface PasswordState {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

const EMPTY_PASSWORD: PasswordState = {
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
};

const EMPTY_PROFILE_FORM: ProfileFormData = {
  name: "",
  phone: "",
  address: "",
  dateOfBirth: "",
  nationality: "",
  jobTitle: "",
  department: "",
  employmentType: "",
  workLocation: "",
  workingHours: "",
  bankName: "",
  bankBranch: "",
  accountNumber: "",
  accountHolderName: "",
  accountType: "",
  iban: "",
};

const createProfileForm = (
  profile: EmployeeProfile
): ProfileFormData => ({
  name: profile.name || "",
  phone: profile.phone || "",
  address: profile.address || "",
  dateOfBirth: profile.dateOfBirth || "",
  nationality: profile.nationality || "",
  jobTitle: profile.jobTitle || "",
  department: profile.department || "",
  employmentType: profile.employmentType || "",
  workLocation: profile.workLocation || "",
  workingHours: profile.workingHours || "",
  bankName: profile.bankName || "",
  bankBranch: profile.bankBranch || "",
  accountNumber: profile.accountNumber || "",
  accountHolderName:
    profile.accountHolderName || "",
  accountType: profile.accountType || "",
  iban: profile.iban || "",
});

const getToken = (): string | null => {
  return localStorage.getItem("token");
};

const getAuthHeaders = (): Record<string, string> => {
  const token = getToken();

  if (!token) {
    return {};
  }

  return {
    Authorization: `Bearer ${token}`,
  };
};

const getErrorMessage = (
  error: unknown,
  fallback: string
): string => {
  if (axios.isAxiosError(error)) {
    const message =
      error.response?.data?.message;

    if (typeof message === "string") {
      return message;
    }
  }

  return fallback;
};

const Profile = () => {
  const { t, i18n } = useTranslation();

  const isArabic =
    i18n.language?.startsWith("ar");

  const direction = isArabic
    ? "rtl"
    : "ltr";

  const [profile, setProfile] =
    useState<EmployeeProfile | null>(null);

  const [documents, setDocuments] =
    useState<DocumentItem[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [documentsLoading, setDocumentsLoading] =
    useState(false);

  const [profileError, setProfileError] =
    useState<string | null>(null);

  const [activeTab, setActiveTab] =
    useState<TabType>("overview");

  const [editing, setEditing] =
    useState(false);

  const [changingPassword, setChangingPassword] =
    useState(false);

  const [savingProfile, setSavingProfile] =
    useState(false);

  const [savingPassword, setSavingPassword] =
    useState(false);

  const [uploading, setUploading] =
    useState(false);

  const [form, setForm] =
    useState<ProfileFormData>(
      EMPTY_PROFILE_FORM
    );

  const [passwordForm, setPasswordForm] =
    useState<PasswordState>(
      EMPTY_PASSWORD
    );

  // =========================================================
  // LOAD PROFILE
  // =========================================================

  const loadProfile = async () => {
    try {
      setLoading(true);
      setProfileError(null);

      const response = await axios.get(
        `${API_URL}/employee-profile`,
        {
          headers: getAuthHeaders(),
        }
      );

      const data: EmployeeProfile =
        response.data;
      console.log("PROFILE JOB TITLE RAW:", {
        json: JSON.stringify(data?.jobTitle),
        value: data?.jobTitle,
        type: typeof data?.jobTitle,
      });


      console.log(
        "========== PROFILE RESPONSE =========="
      );
      console.log(
        "PROFILE RESPONSE DATA:",
        data
      );
      console.log(
        "PROFILE NAME:",
        data?.name
      );
      console.log(
        "PROFILE EMAIL:",
        data?.email
      );
      console.log(
        "PROFILE ROLE:",
        data?.role
      );
      console.log(
        "PROFILE ROLE TYPE:",
        typeof data?.role
      );
      console.log(
        "======================================"
      );

      setProfile(data);
      setForm(createProfileForm(data));
    } catch (error) {
      console.error(
        "Failed to load profile:",
        error
      );

      if (axios.isAxiosError(error)) {
        const status =
          error.response?.status;

        if (status === 401) {
          setProfileError(
            t(
              "profile.errors.sessionExpired"
            )
          );
        } else if (status === 403) {
          setProfileError(
            t(
              "profile.errors.forbidden"
            )
          );
        } else if (status === 404) {
          setProfileError(
            t(
              "profile.errors.notFound"
            )
          );
        } else if (status === 500) {
          setProfileError(
            t(
              "profile.errors.server"
            )
          );
        } else {
          setProfileError(
            getErrorMessage(
              error,
              t("profile.errors.load")
            )
          );
        }
      } else {
        setProfileError(
          t("profile.errors.unexpected")
        );
      }
    } finally {
      setLoading(false);
    }
  };

  // =========================================================
  // LOAD DOCUMENTS
  // =========================================================

  const loadDocuments = async () => {
    try {
      setDocumentsLoading(true);

      const response = await axios.get(
        `${API_URL}/profile/documents`,
        {
          headers: getAuthHeaders(),
        }
      );

      const data = response.data;

      if (Array.isArray(data)) {
        setDocuments(data);
      } else if (
        data &&
        Array.isArray(data.documents)
      ) {
        setDocuments(data.documents);
      } else {
        setDocuments([]);
      }
    } catch (error) {
      console.error(
        "Failed to load documents:",
        error
      );

      setDocuments([]);
    } finally {
      setDocumentsLoading(false);
    }
  };

  // =========================================================
  // INITIAL LOAD
  // =========================================================

  useEffect(() => {
    const loadData = async () => {
      await loadProfile();
      await loadDocuments();
    };

    void loadData();

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // =========================================================
  // SAVE PROFILE
  // =========================================================

  const handleSaveProfile = async () => {
    if (!form.name.trim()) {
      alert(
        t("profile.validation.nameRequired")
      );

      return;
    }

    try {
      setSavingProfile(true);

      const payload = {
        name: form.name.trim(),
        phone: form.phone.trim(),
        address: form.address.trim(),
        dateOfBirth: form.dateOfBirth,
        nationality: form.nationality.trim(),

        bankName: form.bankName.trim(),
        bankBranch: form.bankBranch.trim(),
        accountNumber:
          form.accountNumber.trim(),
        accountHolderName:
          form.accountHolderName.trim(),
        accountType: form.accountType,
        iban: form.iban.trim(),
      };

      const response = await axios.put(
        `${API_URL}/employee-profile`,
        payload,
        {
          headers: {
            ...getAuthHeaders(),
            "Content-Type":
              "application/json",
          },
        }
      );

      const updatedProfile: EmployeeProfile =
        response.data;

      console.log(
        "UPDATED PROFILE RESPONSE:",
        updatedProfile
      );

      console.log(
        "UPDATED PROFILE ROLE:",
        updatedProfile?.role
      );

      setProfile(updatedProfile);

      setForm(
        createProfileForm(
          updatedProfile
        )
      );

      setEditing(false);

      alert(
        t(
          "profile.messages.profileUpdated"
        )
      );
    } catch (error) {
      console.error(
        "Failed to update profile:",
        error
      );

      alert(
        getErrorMessage(
          error,
          t("profile.errors.update")
        )
      );
    } finally {
      setSavingProfile(false);
    }
  };

  // =========================================================
  // CHANGE PASSWORD
  // =========================================================

  const handleChangePassword =
    async () => {
      if (
        !passwordForm.currentPassword.trim()
      ) {
        alert(
          t(
            "profile.validation.currentPassword"
          )
        );

        return;
      }

      if (
        !passwordForm.newPassword.trim()
      ) {
        alert(
          t(
            "profile.validation.newPassword"
          )
        );

        return;
      }

      if (
        passwordForm.newPassword.length < 6
      ) {
        alert(
          t(
            "profile.validation.passwordMin"
          )
        );

        return;
      }

      if (
        !passwordForm.confirmPassword.trim()
      ) {
        alert(
          t(
            "profile.validation.confirmPassword"
          )
        );

        return;
      }

      if (
        passwordForm.newPassword !==
        passwordForm.confirmPassword
      ) {
        alert(
          t(
            "profile.validation.passwordMismatch"
          )
        );

        return;
      }

      try {
        setSavingPassword(true);

        await axios.put(
          `${API_URL}/employee-profile/change-password`,
          {
            currentPassword:
              passwordForm.currentPassword,
            newPassword:
              passwordForm.newPassword,
          },
          {
            headers: {
              ...getAuthHeaders(),
              "Content-Type":
                "application/json",
            },
          }
        );

        setPasswordForm(
          EMPTY_PASSWORD
        );

        setChangingPassword(false);

        alert(
          t(
            "profile.messages.passwordUpdated"
          )
        );
      } catch (error) {
        console.error(
          "Failed to change password:",
          error
        );

        alert(
          getErrorMessage(
            error,
            t(
              "profile.errors.passwordUpdate"
            )
          )
        );
      } finally {
        setSavingPassword(false);
      }
    };

  // =========================================================
  // UPLOAD DOCUMENT
  // =========================================================

  const handleUpload = async (
    event: ChangeEvent<HTMLInputElement>
  ) => {
    const file =
      event.target.files?.[0];

    if (!file) {
      return;
    }

    if (
      file.size >
      5 * 1024 * 1024
    ) {
      alert(
        t("profile.validation.fileSize")
      );

      event.target.value = "";

      return;
    }

    try {
      setUploading(true);

      const formData = new FormData();

      formData.append(
        "file",
        file
      );

      await axios.post(
        `${API_URL}/profile/documents/attachment`,
        formData,
        {
          headers: {
            ...getAuthHeaders(),
          },
        }
      );

      event.target.value = "";

      await loadDocuments();

      alert(
        t(
          "profile.messages.documentUploaded"
        )
      );
    } catch (error) {
      console.error(
        "Failed to upload document:",
        error
      );

      alert(
        getErrorMessage(
          error,
          t(
            "profile.errors.documentUpload"
          )
        )
      );

      event.target.value = "";
    } finally {
      setUploading(false);
    }
  };

  // =========================================================
  // VIEW DOCUMENT
  // =========================================================

  const handleViewDocument = async (
    documentId: number
  ) => {
    // Open the tab immediately during the user's click.
    // This avoids mobile browser popup blockers after an async request.
    const documentWindow = window.open("about:blank", "_blank");

    if (!documentWindow) {
      alert(
        t("profile.errors.documentOpen")
      );
      return;
    }

    documentWindow.opener = null;

    try {
      const response =
        await axios.get(
          `${API_URL}/profile/documents/${documentId}/download`,
          {
            headers:
              getAuthHeaders(),
            responseType: "blob",
          }
        );

      const contentTypeHeader = response.headers["content-type"];
      const contentType =
        typeof contentTypeHeader === "string"
          ? contentTypeHeader
          : "application/octet-stream";

      const blob = new Blob(
        [response.data],
        { type: contentType }
      );

      const url =
        window.URL.createObjectURL(
          blob
        );

      documentWindow.location.replace(url);

      // Keep the object URL available longer for mobile document viewers.
      window.setTimeout(
        () => {
          window.URL.revokeObjectURL(url);
        },
        300000
      );
    } catch (error) {
      documentWindow.close();

      console.error(
        "Failed to open document:",
        error
      );

      alert(
        getErrorMessage(
          error,
          t(
            "profile.errors.documentOpen"
          )
        )
      );
    }
  };

  // =========================================================
  // EDIT PROFILE
  // =========================================================

  const openEditModal = () => {
    if (!profile) {
      return;
    }

    setForm(
      createProfileForm(profile)
    );

    setEditing(true);
  };

  const closeEditModal = () => {
    if (savingProfile) {
      return;
    }

    if (profile) {
      setForm(
        createProfileForm(profile)
      );
    }

    setEditing(false);
  };

  // =========================================================
  // PASSWORD MODAL
  // =========================================================

  const openPasswordModal = () => {
    setPasswordForm(
      EMPTY_PASSWORD
    );

    setChangingPassword(true);
  };

  const closePasswordModal = () => {
    if (savingPassword) {
      return;
    }

    setPasswordForm(
      EMPTY_PASSWORD
    );

    setChangingPassword(false);
  };

  // =========================================================
  // DOCUMENTS TAB
  // =========================================================

  const handleDocumentsTab = () => {
    setActiveTab("documents");

    void loadDocuments();
  };

  // =========================================================
  // LOADING
  // =========================================================

  if (loading) {
    return (
      <div
        dir={direction}
        className="
          sahab-profile-page relative
          min-h-screen
          w-full
          overflow-hidden
          bg-slate-50
          text-slate-900
          dark:bg-[#020617]
          dark:text-white
        "
      >


        <main className="min-h-screen">
          <div className="relative min-h-screen flex items-center justify-center px-4">
            <div className="pointer-events-none absolute left-1/2 top-1/2 h-[420px] w-[420px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-cyan-400/10 blur-[120px] dark:bg-cyan-500/[0.06]" />

            <motion.div
              initial={{
                opacity: 0,
                y: 15,
                scale: 0.96,
              }}
              animate={{
                opacity: 1,
                y: 0,
                scale: 1,
              }}
              className="
                relative
                flex
                flex-col
                items-center
                gap-5
                rounded-[30px]
                border
                border-slate-200
                bg-white/90
                px-10
                py-9
                shadow-xl
                shadow-slate-900/5
                backdrop-blur-xl
                dark:border-white/[0.08]
                dark:bg-white/[0.035]
                dark:shadow-black/20
              "
            >
              <div
                className="
                  relative
                  flex
                  h-16
                  w-16
                  items-center
                  justify-center
                  rounded-2xl
                  border
                  border-cyan-500/20
                  bg-cyan-500/10
                "
              >
                <Loader2
                  className="
                    h-7
                    w-7
                    animate-spin
                    text-cyan-500
                    dark:text-cyan-400
                  "
                />

                <div className="absolute -inset-2 rounded-3xl border border-cyan-500/10 animate-pulse" />
              </div>

              <div className="text-center">
                <p className="font-bold text-slate-800 dark:text-white">
                  {t("profile.loading")}
                </p>

                <p className="mt-1 text-xs text-slate-500 dark:text-slate-500">
                  {isArabic
                    ? "جاري تحميل بيانات حسابك"
                    : "Loading your account information"}
                </p>
              </div>
            </motion.div>
          </div>
        </main>
      </div>
    );
  }

  // =========================================================
  // ERROR
  // =========================================================

  if (
    profileError ||
    !profile
  ) {
    return (
      <div
        dir={direction}
        className="
          sahab-profile-page relative
          min-h-screen
          w-full
          overflow-hidden
          bg-slate-50
          text-slate-900
          dark:bg-[#020617]
          dark:text-white
        "
      >


        <main className="min-h-screen">
          <div className="relative min-h-screen flex items-center justify-center px-4">
            <div className="pointer-events-none absolute left-1/2 top-1/2 h-[450px] w-[500px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-red-500/5 blur-[120px]" />

            <motion.div
              initial={{
                opacity: 0,
                y: 18,
                scale: 0.97,
              }}
              animate={{
                opacity: 1,
                y: 0,
                scale: 1,
              }}
              className="
                relative
                w-full
                max-w-lg
                rounded-[30px]
                border
                border-red-200
                bg-white/90
                p-7
                text-center
                shadow-xl
                shadow-slate-900/5
                backdrop-blur-xl
                dark:border-red-500/20
                dark:bg-white/[0.035]
                dark:shadow-black/20
                sm:p-9
              "
            >
              <div
                className="
                  mx-auto
                  flex
                  h-16
                  w-16
                  items-center
                  justify-center
                  rounded-2xl
                  bg-red-500/10
                "
              >
                <AlertCircle
                  className="
                    h-8
                    w-8
                    text-red-500
                    dark:text-red-400
                  "
                />
              </div>

              <h2
                className="
                  mt-5
                  text-xl
                  font-black
                  text-slate-900
                  dark:text-white
                  sm:text-2xl
                "
              >
                {t(
                  "profile.errors.title"
                )}
              </h2>

              <p
                className="
                  mt-3
                  text-sm
                  leading-7
                  text-slate-500
                  dark:text-slate-400
                "
              >
                {profileError ||
                  t(
                    "profile.errors.load"
                  )}
              </p>

              <button
                type="button"
                onClick={() => {
                  void loadProfile();
                }}
                className="
                  mt-7
                  inline-flex
                  items-center
                  justify-center
                  gap-2
                  rounded-2xl
                  bg-gradient-to-r
                  from-cyan-500
                  to-indigo-600
                  px-6
                  py-3.5
                  font-bold
                  text-white
                  shadow-lg
                  shadow-cyan-500/20
                  transition-all
                  duration-200
                  hover:-translate-y-0.5
                  hover:from-cyan-400
                  hover:to-indigo-500
                "
              >
                <RefreshCw size={18} />

                {t(
                  "profile.actions.tryAgain"
                )}
              </button>
            </motion.div>
          </div>
        </main>
      </div>
    );
  }

  // =========================================================
  // MAIN PROFILE
  // =========================================================

  return (
    <div
      dir={direction}
      className="
        sahab-profile-page relative
        min-h-screen
        w-full
        min-w-0
        overflow-x-hidden

        text-slate-900
        transition-colors
        duration-300

        dark:text-white
      "
    >


      {/* =====================================================
          AMBIENT BACKGROUND
      ===================================================== */}

      <div className="pointer-events-none fixed inset-0 overflow-hidden">
        <div className="absolute -top-32 left-1/3 h-[380px] w-[380px] rounded-full bg-cyan-400/[0.035] blur-[120px] dark:bg-cyan-500/[0.05]" />

        <div className="absolute top-1/3 -right-32 h-[380px] w-[380px] rounded-full bg-indigo-500/[0.025] blur-[120px] dark:bg-indigo-500/[0.04]" />

        <div className="absolute bottom-0 left-1/4 h-[320px] w-[320px] rounded-full bg-violet-500/[0.02] blur-[120px] dark:bg-violet-500/[0.035]" />

        <div
          className="
            absolute
            inset-0
            opacity-[0.015]
            dark:opacity-[0.025]
            [background-image:linear-gradient(rgba(100,116,139,0.35)_1px,transparent_1px),linear-gradient(90deg,rgba(100,116,139,0.35)_1px,transparent_1px)]
            [background-size:64px_64px]
          "
        />
      </div>

      {/* =====================================================
          CONTENT
      ===================================================== */}

      <main className="relative z-10 min-h-screen">
        <div
          className="
            relative
            mx-auto
            w-full
            min-w-0
            max-w-[1700px]
            px-3
            py-4
            sm:px-5
            sm:py-5
            lg:px-7
            lg:py-7
            xl:px-8
          "
        >
          {/* =================================================
              PAGE INTRO
          ================================================= */}

          <motion.div
            initial={{
              opacity: 0,
              y: 10,
            }}
            animate={{
              opacity: 1,
              y: 0,
            }}
            transition={{
              duration: 0.35,
            }}
            className="
              mb-5
              flex
              flex-col
              gap-4
              sm:flex-row
              sm:items-center
              sm:justify-between
            "
          >
            <div className="flex items-center gap-3">
              <div
                className="
                  flex
                  h-11
                  w-11
                  items-center
                  justify-center
                  rounded-2xl
                  border
                  border-cyan-500/15
                  bg-cyan-500/10
                "
              >
                <UserRound
                  size={20}
                  className="text-cyan-500 dark:text-cyan-400"
                />
              </div>

              <div>
                <div className="flex items-center gap-2">
                  <Sparkles
                    size={13}
                    className="text-cyan-500 dark:text-cyan-400"
                  />

                  <span className="text-[10px] font-black uppercase tracking-[0.18em] text-cyan-500 dark:text-cyan-400">
                    {isArabic
                      ? "الحساب الشخصي"
                      : "Personal Account"}
                  </span>
                </div>

                <h1 className="mt-0.5 text-2xl font-black tracking-tight text-slate-900 dark:text-white sm:text-3xl">
                  {isArabic
                    ? "ملفي الشخصي"
                    : "My Profile"}
                </h1>
              </div>
            </div>

            <div
              className="
                inline-flex
                w-fit
                items-center
                gap-2
                rounded-full
                border
                border-emerald-500/15
                bg-emerald-500/5
                px-3.5
                py-2
                text-xs
                font-semibold
                text-emerald-600
                dark:text-emerald-400
              "
            >
              <span className="relative flex h-2 w-2">
                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-60" />

                <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-500" />
              </span>

              {isArabic
                ? "الحساب نشط"
                : "Account Active"}
            </div>
          </motion.div>

          {/* =================================================
              PROFILE HEADER
          ================================================= */}

          <motion.div
            initial={{
              opacity: 0,
              y: 15,
            }}
            animate={{
              opacity: 1,
              y: 0,
            }}
            transition={{
              duration: 0.4,
              delay: 0.05,
            }}
            className="w-full min-w-0"
          >
            <ProfileHeader
              profile={profile}
              onEdit={openEditModal}
            />
          </motion.div>

          {/* =================================================
              TABS
          ================================================= */}

          <motion.div
            initial={{
              opacity: 0,
              y: 12,
            }}
            animate={{
              opacity: 1,
              y: 0,
            }}
            transition={{
              duration: 0.4,
              delay: 0.1,
            }}
            className="mt-4 w-full min-w-0 sm:mt-5"
          >
            <div
              className="
                w-full
                min-w-0
                overflow-x-auto
                overscroll-x-contain
                rounded-2xl
              "
            >
              <div className="min-w-max">
                <ProfileTabs
                  activeTab={activeTab}
                  setActiveTab={setActiveTab}
                />
              </div>
            </div>
          </motion.div>

          {/* =================================================
              CONTENT
          ================================================= */}

          <motion.div
            key={activeTab}
            initial={{
              opacity: 0,
              y: 10,
            }}
            animate={{
              opacity: 1,
              y: 0,
            }}
            transition={{
              duration: 0.3,
            }}
            className="mt-5 w-full min-w-0 sm:mt-6"
          >
            {/* =================================================
                OVERVIEW
            ================================================= */}

            {activeTab === "overview" && (
              <div className="w-full min-w-0">
                <ProfileOverview
                  profile={profile}
                  documents={documents}
                  onEdit={openEditModal}
                  onDocuments={
                    handleDocumentsTab
                  }
                  onSecurity={() =>
                    setActiveTab(
                      "security"
                    )
                  }
                />
              </div>
            )}

            {/* =================================================
                PERSONAL
            ================================================= */}

            {activeTab === "personal" && (
              <div className="w-full min-w-0">
                <ProfilePersonal
                  profile={profile}
                  onEdit={openEditModal}
                />
              </div>
            )}

            {/* =================================================
                EMPLOYMENT
            ================================================= */}

            {activeTab === "employment" && (
              <div className="w-full min-w-0">
                <ProfileEmployment
                  profile={profile}
                  onEdit={openEditModal}
                />
              </div>
            )}

            {/* =================================================
                DOCUMENTS
            ================================================= */}

            {activeTab === "documents" && (
              <div className="relative w-full min-w-0">
                {documentsLoading && (
                  <motion.div
                    initial={{
                      opacity: 0,
                      y: -5,
                    }}
                    animate={{
                      opacity: 1,
                      y: 0,
                    }}
                    className="
                      pointer-events-none
                      absolute
                      top-3
                      z-20
                      flex
                      items-center
                      gap-2
                      rounded-xl
                      border
                      border-slate-200
                      bg-white/95
                      px-3
                      py-2
                      text-xs
                      text-slate-500
                      shadow-xl
                      backdrop-blur-xl
                      dark:border-white/[0.08]
                      dark:bg-[#071225]/95
                      dark:text-slate-400
                      sm:top-4
                    "
                    style={
                      isArabic
                        ? {
                            right: "1rem",
                          }
                        : {
                            left: "1rem",
                          }
                    }
                  >
                    <Loader2 className="h-3.5 w-3.5 animate-spin" />

                    {t(
                      "profile.documents.updating"
                    )}
                  </motion.div>
                )}

                <div className="w-full min-w-0">
                  <ProfileDocuments
                    documents={documents}
                    onUpload={handleUpload}
                    onView={
                      handleViewDocument
                    }
                    uploading={uploading}
                  />
                </div>
              </div>
            )}

            {/* =================================================
                SECURITY
            ================================================= */}

            {activeTab === "security" && (
              <div className="w-full min-w-0">
                <ProfileSecurity
                  profile={profile}
                  onChangePassword={
                    openPasswordModal
                  }
                />
              </div>
            )}
          </motion.div>

          {/* =================================================
              SECURITY FOOTER
          ================================================= */}

          <motion.div
            initial={{
              opacity: 0,
              y: 10,
            }}
            animate={{
              opacity: 1,
              y: 0,
            }}
            transition={{
              duration: 0.4,
              delay: 0.2,
            }}
            className="
              mt-6
              flex
              flex-col
              gap-3
              rounded-2xl
              border
              border-slate-200
              bg-white/70
              p-4
              backdrop-blur-xl
              dark:border-white/[0.07]
              dark:bg-white/[0.025]
              sm:flex-row
              sm:items-center
              sm:justify-between
            "
          >
            <div className="flex items-center gap-3">
              <div
                className="
                  flex
                  h-9
                  w-9
                  shrink-0
                  items-center
                  justify-center
                  rounded-xl
                  bg-emerald-500/10
                "
              >
                <ShieldCheck
                  size={18}
                  className="text-emerald-500 dark:text-emerald-400"
                />
              </div>

              <div>
                <p className="text-xs font-bold text-slate-700 dark:text-slate-300">
                  {isArabic
                    ? "بياناتك محمية"
                    : "Your information is protected"}
                </p>

                <p className="mt-0.5 text-[11px] text-slate-500 dark:text-slate-500">
                  {isArabic
                    ? "يمكنك تعديل بياناتك الشخصية والمصرفية من خلال الملف الشخصي."
                    : "You can update your personal and banking information from your profile."}
                </p>
              </div>
            </div>

            <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400">
               Management System
            </div>
          </motion.div>
        </div>
      </main>

      {/* =====================================================
          EDIT PROFILE MODAL
      ===================================================== */}

      <EditProfileModal
        open={editing}
        profile={profile}
        form={form}
        setForm={setForm}
        onClose={closeEditModal}
        onSave={handleSaveProfile}
        saving={savingProfile}
      />

      {/* =====================================================
          CHANGE PASSWORD MODAL
      ===================================================== */}

      <ChangePasswordModal
        open={changingPassword}
        currentPassword={
          passwordForm.currentPassword
        }
        newPassword={
          passwordForm.newPassword
        }
        confirmPassword={
          passwordForm.confirmPassword
        }
        setCurrentPassword={(value) =>
          setPasswordForm(
            (previous) => ({
              ...previous,
              currentPassword:
                value,
            })
          )
        }
        setNewPassword={(value) =>
          setPasswordForm(
            (previous) => ({
              ...previous,
              newPassword:
                value,
            })
          )
        }
        setConfirmPassword={(value) =>
          setPasswordForm(
            (previous) => ({
              ...previous,
              confirmPassword:
                value,
            })
          )
        }
        onClose={closePasswordModal}
        onSave={handleChangePassword}
        saving={savingPassword}
      />
    </div>
  );
};

export default Profile;


/* PROFILE LIGHT MODE TEXT VISIBILITY FIX */
