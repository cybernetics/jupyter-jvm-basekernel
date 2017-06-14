package io.github.spencerpark.jupyter.messages.request;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.MessageType;

public class HistoryRequest {
    public static final MessageType MESSAGE_TYPE = MessageType.HISTORY_REQUEST;

    public enum AccessType {
        @SerializedName("range") RANGE,
        @SerializedName("tail") TAIL,
        @SerializedName("search") SEARCH,
    }

    private static class Base {
        /**
         * If true, include the output associated with the inputs.
         */
        protected final boolean output;

        /**
         * If true, return the raw input history, else the transformed input.
         */
        protected final boolean raw;

        @SerializedName("hist_access_type")
        protected final AccessType accessType;

        public Base(boolean output, boolean raw, AccessType accessType) {
            this.output = output;
            this.raw = raw;
            this.accessType = accessType;
        }

        public boolean isOutput() {
            return output;
        }

        public boolean isRaw() {
            return raw;
        }

        public AccessType getAccessType() {
            return accessType;
        }
    }

    public static class Range extends Base {
        /**
         * A session index that counts up each time the kernel
         * starts. If negative the number is counting back from
         * the current session.
         */
        protected final int session;

        /**
         * Start cell (execution count number) within the session.
         */
        protected final int start;

        /**
         * Stop cell (execution count number) with the session.
         */
        protected final int stop;

        public Range(boolean output, boolean raw, int session, int start, int stop) {
            super(output, raw, AccessType.RANGE);
            this.session = session;
            this.start = start;
            this.stop = stop;
        }

        public int getSessionIndex() {
            return session;
        }

        public int getStart() {
            return start;
        }

        public int getStop() {
            return stop;
        }
    }

    public static class Tail extends Base {
        /**
         * Get the last n executions
         */
        protected final int n;

        public Tail(boolean output, boolean raw, int n) {
            super(output, raw, AccessType.TAIL);
            this.n = n;
        }

        public int getMaxReturnLength() {
            return n;
        }
    }

    public static class Search extends Base {
        /**
         * Get the last n executions
         */
        protected final int n;

        /**
         * Glob style filter with '*' and '?'. Default to '*'
         */
        protected final String pattern;

        /**
         * If true, omit duplicate entries in the return. Defaults
         * to false.
         */
        protected final boolean unique;

        public Search(boolean output, boolean raw, int n, String pattern, boolean unique) {
            super(output, raw, AccessType.SEARCH);
            this.n = n;
            this.pattern = pattern;
            this.unique = unique;
        }

        public int getMaxReturnLength() {
            return n;
        }

        public String getPattern() {
            return pattern;
        }

        public boolean isUnique() {
            return unique;
        }
    }
}
