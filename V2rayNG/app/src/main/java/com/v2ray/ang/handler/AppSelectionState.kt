package com.v2ray.ang.handler

interface AppSelectionState {
    fun contains(packageName: String): Boolean
    fun getAll(): Set<String>
    fun add(packageName: String): Boolean
    fun remove(packageName: String): Boolean
    fun toggle(packageName: String)
    fun addAll(packages: Collection<String>)
    fun removeAll(packages: Collection<String>)
    fun clear()
}
