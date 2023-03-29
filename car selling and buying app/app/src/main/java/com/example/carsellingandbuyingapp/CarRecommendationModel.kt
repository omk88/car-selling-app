package com.example.carsellingandbuyingapp

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.FloatBuffer
import java.nio.channels.FileChannel

class CarRecommendationModel(context: Context) {
    private val interpreter: Interpreter

    init {
        val assetFileDescriptor = context.assets.openFd("my_model.tflite")
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        interpreter = Interpreter(modelBuffer)
    }


    fun predict(input: FloatArray): FloatArray {
        val inputShape = interpreter.getInputTensor(0).shape()
        val outputShape = interpreter.getOutputTensor(0).shape()

        val inputBuffer = FloatBuffer.allocate(inputShape[0] * inputShape[1])
        val outputBuffer = FloatBuffer.allocate(outputShape[0] * outputShape[1])

        inputBuffer.put(input).rewind()

        interpreter.run(inputBuffer, outputBuffer)

        val outputArray = FloatArray(outputShape[1])
        outputBuffer.rewind()
        outputBuffer.get(outputArray)

        return outputArray
    }



}

