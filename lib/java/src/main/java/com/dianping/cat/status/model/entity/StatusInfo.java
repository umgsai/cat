package com.dianping.cat.status.model.entity;

import com.dianping.cat.status.model.transform.DefaultXmlBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class StatusInfo extends BaseEntity<StatusInfo> {
    private Date timestamp;
    private RuntimeInfo runtime;
    private OsInfo os;
    private DiskInfo disk;
    private MemoryInfo memory;
    private ThreadsInfo thread;
    private MessageInfo message;
    private final Map<String, Extension> extensions = new LinkedHashMap<String, Extension>();

    public StatusInfo() {
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitStatus(this);
    }

    public StatusInfo addExtension(Extension extension) {
        extensions.put(extension.getId(), extension);
        return this;
    }

    public Extension findExtension(String id) {
        return extensions.get(id);
    }

    public Extension findOrCreateExtension(String id) {
        Extension extension = extensions.get(id);

        if (extension == null) {
            synchronized (extensions) {
                extension = extensions.get(id);

                if (extension == null) {
                    extension = new Extension(id);
                    extensions.put(id, extension);
                }
            }
        }

        return extension;
    }

    @Override
    public void mergeAttributes(StatusInfo other) {
        if (other.getTimestamp() != null) {
            timestamp = other.getTimestamp();
        }
    }

    public boolean removeExtension(String id) {
        if (extensions.containsKey(id)) {
            extensions.remove(id);
            return true;
        }

        return false;
    }

    public StatusInfo setDisk(DiskInfo disk) {
        this.disk = disk;
        return this;
    }

    public StatusInfo setMemory(MemoryInfo memory) {
        this.memory = memory;
        return this;
    }

    public StatusInfo setMessage(MessageInfo message) {
        this.message = message;
        return this;
    }

    public StatusInfo setOs(OsInfo os) {
        this.os = os;
        return this;
    }

    public StatusInfo setRuntime(RuntimeInfo runtime) {
        this.runtime = runtime;
        return this;
    }

    public StatusInfo setThread(ThreadsInfo thread) {
        this.thread = thread;
        return this;
    }

    public StatusInfo setTimestamp(java.util.Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return new DefaultXmlBuilder().buildXml(this);
    }
}
