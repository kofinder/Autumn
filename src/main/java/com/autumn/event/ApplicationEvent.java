package com.autumn.event;

public abstract class ApplicationEvent {
    private final Object source;

    public ApplicationEvent(Object source) {
        this.source = source;
    }

    public Object getSource() {
        return source;
    }

}
