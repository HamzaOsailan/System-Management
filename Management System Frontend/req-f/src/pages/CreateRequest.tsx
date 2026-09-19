import { useState } from "react";
import toast from "react-hot-toast";
import { motion } from "framer-motion";
import {
  Send,
  FileText,
  MessageSquareText,
  ClipboardCheck,
  Clock,
  CheckCircle2,
  Tag,
} from "lucide-react";

import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";

import api from "../api/axios";

const DESCRIPTION_LIMIT = 500;

const CATEGORIES = [
  { value: "GENERAL", label: "General" },
  { value: "IT", label: "IT" },
  { value: "LEAVE", label: "Leave" },
  { value: "HR", label: "HR" },
];

function CreateRequest() {
  const [title, setTitle] = useState("");
  const [category, setCategory] = useState("GENERAL");
  const [description, setDescription] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const createRequest = async () => {
    if (!title.trim() || !description.trim()) {
      toast.error("Please fill in both title and description");
      return;
    }

    setSubmitting(true);

    try {
      const token = localStorage.getItem("token");

      await api.post(
        "/requests/CreateRequest",
        {
          title,
          description,
          category,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      toast.success("Request created successfully");

      setTitle("");
      setDescription("");
      setCategory("GENERAL");
    } catch (error) {
      console.log(error);
      toast.error("Failed to create request");
    } finally {
      setSubmitting(false);
    }
  };

  const steps = [
    {
      icon: <ClipboardCheck size={18} />,
      label: "You submit",
      desc: "Title and details go straight into the queue",
    },
    {
      icon: <Clock size={18} />,
      label: "Admin reviews",
      desc: "Usually looked at within a day",
    },
    {
      icon: <CheckCircle2 size={18} />,
      label: "You get notified",
      desc: "Approved, or sent back with a reason",
    },
  ];

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
      {/* SIDEBAR */}
      <Sidebar />

      {/* MAIN */}
      <div className="flex-1">
        {/* NAVBAR */}
        <Navbar />

        {/* CONTENT */}
        <div className="p-8">
          <div className="mb-10">
            <h1 className="text-5xl font-black tracking-tight">
              Create Request
            </h1>
            <p className="text-gray-400 mt-2 text-lg">
              Submit a new request for review
            </p>
          </div>

          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4, ease: "easeOut" }}
            className="
              grid
              grid-cols-1
              lg:grid-cols-[320px_1fr]
              gap-6
              max-w-4xl
            "
          >
            {/* CONTEXT PANEL */}
            <div
              className="
                relative
                overflow-hidden
                bg-white/5
                border
                border-white/10
                backdrop-blur-2xl
                rounded-[32px]
                p-8
              "
            >
              <div
                className="
                  absolute
                  top-[-80px]
                  right-[-80px]
                  w-[220px]
                  h-[220px]
                  bg-blue-500/10
                  rounded-full
                  blur-3xl
                "
              />

              <div className="relative z-10">
                <div
                  className="
                    w-14
                    h-14
                    rounded-2xl
                    bg-gradient-to-r
                    from-blue-500
                    to-cyan-400
                    flex
                    items-center
                    justify-center
                    shadow-[0_0_30px_rgba(59,130,246,0.35)]
                    mb-6
                  "
                >
                  <FileText size={26} />
                </div>

                <h2 className="text-xl font-bold leading-snug">
                  What happens after you hit submit
                </h2>

                <div className="flex flex-col gap-5 mt-6">
                  {steps.map((step, i) => (
                    <div key={step.label} className="flex gap-3">
                      <div className="flex flex-col items-center">
                        <div
                          className="
                            w-9
                            h-9
                            rounded-xl
                            bg-white/10
                            border
                            border-white/10
                            flex
                            items-center
                            justify-center
                            text-blue-300
                            shrink-0
                          "
                        >
                          {step.icon}
                        </div>

                        {i < steps.length - 1 && (
                          <div className="w-px flex-1 bg-white/10 my-1" />
                        )}
                      </div>

                      <div className="pb-1">
                        <p className="font-semibold text-sm">
                          {step.label}
                        </p>
                        <p className="text-gray-400 text-xs mt-1">
                          {step.desc}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* FORM */}
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
              <div className="flex flex-col gap-6">
                <div className="flex flex-col gap-2">
                  <label
                    className="
                      text-gray-300
                      text-sm
                      font-semibold
                      flex
                      items-center
                      gap-2
                    "
                  >
                    <FileText size={15} className="text-blue-400" />
                    Title
                  </label>
                  <input
                    type="text"
                    placeholder="e.g. Fix printer"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    className="
                      bg-white/5
                      border
                      border-white/10
                      rounded-2xl
                      px-5
                      py-3
                      text-white
                      outline-none
                      placeholder:text-gray-500
                      focus:border-blue-400/50
                      transition
                    "
                  />
                </div>

                <div className="flex flex-col gap-2">
                  <label
                    className="
                      text-gray-300
                      text-sm
                      font-semibold
                      flex
                      items-center
                      gap-2
                    "
                  >
                    <Tag size={15} className="text-blue-400" />
                    Category
                  </label>
                  <select
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                    className="
                      bg-white/5
                      border
                      border-white/10
                      rounded-2xl
                      px-5
                      py-3
                      text-white
                      outline-none
                      cursor-pointer
                      focus:border-blue-400/50
                      transition
                    "
                  >
                    {CATEGORIES.map((c) => (
                      <option
                        key={c.value}
                        value={c.value}
                        className="bg-[#0f172a] text-white"
                      >
                        {c.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="flex flex-col gap-2">
                  <div className="flex items-center justify-between">
                    <label
                      className="
                        text-gray-300
                        text-sm
                        font-semibold
                        flex
                        items-center
                        gap-2
                      "
                    >
                      <MessageSquareText size={15} className="text-blue-400" />
                      Description
                    </label>
                    <span className="text-xs text-gray-500">
                      {description.length}/{DESCRIPTION_LIMIT}
                    </span>
                  </div>
                  <textarea
                    placeholder="Describe your request in detail..."
                    value={description}
                    maxLength={DESCRIPTION_LIMIT}
                    onChange={(e) => setDescription(e.target.value)}
                    className="
                      bg-white/5
                      border
                      border-white/10
                      rounded-2xl
                      px-5
                      py-3
                      text-white
                      outline-none
                      h-[160px]
                      resize-none
                      placeholder:text-gray-500
                      focus:border-blue-400/50
                      transition
                    "
                  />
                </div>

                <button
                  onClick={createRequest}
                  disabled={submitting}
                  className="
                    flex
                    items-center
                    justify-center
                    gap-2
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
                  {submitting ? (
                    "Submitting..."
                  ) : (
                    <>
                      <Send size={18} />
                      Create Request
                    </>
                  )}
                </button>
              </div>
            </div>
          </motion.div>
        </div>
      </div>
    </div>
  );
}

export default CreateRequest;