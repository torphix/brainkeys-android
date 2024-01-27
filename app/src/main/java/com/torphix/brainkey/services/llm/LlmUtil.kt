package com.torphix.brainkey.services.llm

object LlmUtil {
    fun formatPrompt(prompt:String, modelName:String):String{
        return when (modelName) {
            "stablelm-2-zephyr-1_6b-Q5_K_M.gguf" ->  "<|user|>\n$prompt<|endoftext|>\n<|assistant|>\n"
            "tinyllama-1.1-f16.gguf" ->   "<|im_start|>user\n${prompt}<|im_end|>\n<|im_start|>assistant\n"
            "phi-2-q4_0.gguf" -> prompt
            else ->  "<|user|>\n$prompt<|endoftext|>\n<|assistant|>\n"
        }
    }
}

