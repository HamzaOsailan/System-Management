import { motion } from "framer-motion";

type Props = {
  request: any;
  onView: (request: any) => void;
};

function RequestCard({
  request,
  onView,
}: Props) {
  return (
    <motion.div
      initial={{
        opacity: 0,
        y: 20,
      }}
      animate={{
        opacity: 1,
        y: 0,
      }}
      whileHover={{
        scale: 1.02,
        y: -5,
      }}
      transition={{
        duration: 0.3,
      }}
      className="
        bg-white/5
        border
        border-white/10
        rounded-[28px]
        p-6
        backdrop-blur-2xl
        shadow-xl
        hover:border-blue-500/30
        transition-all
      "
    >

      <div className="flex items-center justify-between gap-5">


        <div className="min-w-0">

          <h2
            className="
              text-2xl
              font-bold
              text-white
            "
          >
            {request.title || "Untitled Request"}
          </h2>

          <p
            className="
              text-gray-400
              mt-2
              line-clamp-2
            "
          >
            {request.description ||
              "No description available"}
          </p>

        </div>


        <span
          className={`
            shrink-0
            px-4
            py-2
            rounded-2xl
            text-sm
            font-semibold

            ${
              request.status === "APPROVED"
                ? "bg-green-500/20 text-green-400"
                : request.status === "PENDING"
                ? "bg-yellow-500/20 text-yellow-300"
                : "bg-red-500/20 text-red-400"
            }
          `}
        >
          {request.status || "UNKNOWN"}
        </span>

      </div>


      <div className="flex gap-4 mt-6">

        <button
          onClick={() => onView(request)}
          className="
            bg-blue-500
            hover:bg-blue-600
            transition
            px-5
            py-2
            rounded-2xl
            font-semibold
          "
        >
          View
        </button>

      </div>

    </motion.div>
  );
}

export default RequestCard;