package com.soda1127.example.kspimplementation

import com.soda1127.example.kspimplementation.processor.InterfaceImplementation

@InterfaceImplementation
class ExampleRepository {

    fun getData(a: Int, b: Int): Int {
        return a + b
    }

}
