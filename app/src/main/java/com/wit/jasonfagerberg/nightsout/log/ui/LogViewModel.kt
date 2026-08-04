package com.wit.jasonfagerberg.nightsout.log.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wit.jasonfagerberg.nightsout.database.NightsOutRepository
import com.wit.jasonfagerberg.nightsout.models.LogHeader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LogViewModel(
    private val repository: NightsOutRepository
) : ViewModel() {

    private val _headers = MutableStateFlow(ArrayList<LogHeader>())
    val headers: StateFlow<ArrayList<LogHeader>> = _headers

    init {
        viewModelScope.launch { _headers.value = repository.pullLogHeaders() }
    }

    fun isEmpty(): Boolean = _headers.value.isEmpty()

    fun getHeaderIndex(date: Int): Int = _headers.value.indexOf(LogHeader(date))

    fun findHeader(date: Int): LogHeader? = _headers.value.find { it.date == date }

    fun addHeader(header: LogHeader) {
        _headers.value.add(header)
    }

    fun removeHeader(header: LogHeader) {
        _headers.value.remove(header)
    }

    fun removeAll() {
        _headers.value.clear()
    }

    suspend fun updateHeaderAt(index: Int, newHeader: LogHeader) {
        if (index in _headers.value.indices) _headers.value[index] = newHeader
    }
}
