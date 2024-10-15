package com.aserto.directory.v3;

public class ImportEvent {
    public static enum Type {
        UNKNOWN,
        OBJECT,
        RELATION
    }

    public static final class Counter {
        public final long recv;
        public final long set;
        public final long delete;
        public final long error;
        public final Type type;

        public Counter(Type type, long recv, long set, long delete, long error) {
            this.type = type;
            this.recv = recv;
            this.set = set;
            this.delete = delete;
            this.error = error;
        }

        public Counter(com.aserto.directory.importer.v3.ImportCounter counter) {
            switch (counter.getType()) {
                case "object":
                    this.type = Type.OBJECT;
                    break;
                case "relation":
                    this.type = Type.RELATION;
                    break;
                default:
                    this.type = Type.UNKNOWN;
                    break;
            }
            this.recv = counter.getRecv();
            this.set = counter.getSet();
            this.delete = counter.getDelete();
            this.error = counter.getError();
        }
    }

    public static final class Error {
        public final int code;
        public final String message;
        public final com.aserto.directory.importer.v3.ImportRequest request;

        public Error(com.aserto.directory.importer.v3.ImportStatus status) {
            this.code = status.getCode();
            this.message = status.getMsg();
            this.request = status.getReq();
        }
    }
}
