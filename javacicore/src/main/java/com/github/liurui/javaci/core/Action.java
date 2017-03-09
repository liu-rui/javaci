package com.github.liurui.javaci.core;


@FunctionalInterface
public interface Action<T> {
    T execute() throws  Throwable;
}