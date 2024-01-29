package com.torphix.brainkey.services.llm

object LlmUtil {
    fun formatPrompt(systemPrompt:String, userPrompt:String, modelName:String):String{
        return when (modelName) {
            "stablelm-2-zephyr-1_6b-Q5_K_M.gguf" ->  "<|user|>\n$userPrompt Requirements:\n$systemPrompt<|endoftext|>\n<|assistant|>\n"
            "tinyllama-1.1-f16.gguf" ->   "<|im_start|>user\n${userPrompt} Requirements:\n" +
                    "$systemPrompt<|im_end|>\n<|im_start|>assistant\n"
            "phi-2-q4_0.gguf" -> "$userPrompt Requirements:\n" +
                    "$systemPrompt"
            else ->  "<|user|>\n$userPrompt Requirements:\n$systemPrompt<|endoftext|>\n<|assistant|>\n"
        }
    }
}

