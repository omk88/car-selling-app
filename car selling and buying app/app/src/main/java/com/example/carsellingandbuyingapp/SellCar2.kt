package com.example.carsellingandbuyingapp


import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

import org.tensorflow.lite.DataType
import org.tensorflow.lite.Tensor
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.properties.Delegates


class SellCar2 : AppCompatActivity(), View.OnClickListener {

    private lateinit var tflite: Interpreter
    private val modelFile = "car_classifier_model.tflite"

    private lateinit var pred_colour: String
    private lateinit var pred_make: String
    private lateinit var pred_year: String

    var vehicleData: VehicleData? = null
    private var RESULT_LOAD_IMAGE: Int = 1
    private var imageUri0: Uri? = null
    private var imageUri1: Uri? = null
    private var imageUri2: Uri? = null
    private var imageNumber: Int = 0
    private lateinit var textView: TextView
    private lateinit var selectModel: EditText
    private val img_height = 224
    private val img_width = 224
    private lateinit var errorMessage: TextView
    private lateinit var priceText: EditText
    private var predictedPrice by Delegates.notNull<Float>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_car2)

        val priceText = findViewById<EditText>(R.id.editTextCarPrice)

        priceText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isNotEmpty() && s.toString().substring(0, 1) == "£" && count == 1 && after == 0) {
                    priceText.setText("£")
                    priceText.setSelection(priceText.text.length)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty() && s.toString().substring(0, 1) != "£") {
                    priceText.setText("£" + s.toString())
                    priceText.setSelection(priceText.text.length)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        val loggedInUser = application as Username

        val goBack = findViewById<LinearLayout>(R.id.goBack)

        goBack.setOnClickListener {
            val intent = Intent(this, SellCar::class.java)
            intent.putExtra("username", loggedInUser.username)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }


        initTfliteInterpreter("car_classifier_model.tflite")

        textView = findViewById(R.id.textView3)

        val image0 = findViewById<ImageView>(R.id.upload0)
        image0.setOnClickListener(this)
        val image1 = findViewById<ImageView>(R.id.upload1)
        image1.setOnClickListener(this)
        val image2 = findViewById<ImageView>(R.id.upload2)
        image2.setOnClickListener(this)

        image0.setImageURI(imageUri0)
        image1.setImageURI(imageUri1)
        image2.setImageURI(imageUri2)


        val sellCarButton = findViewById<LinearLayout>(R.id.postCar)
        sellCarButton.setOnClickListener(this)

        selectModel = findViewById(R.id.selectModel)

        val condition = intent.getStringExtra("condition")
        val price = priceText.text.toString()
        val mileage = intent.getStringExtra("mileage")
        val description = intent.getStringExtra("description")

        if (intent.getStringExtra("selected_model") != null) {
            selectModel.setText(intent.getStringExtra("selected_model").toString())

            val tfliteModel = loadModelFile2(this, "price_recommendation_model.tflite")
            val tflite = Interpreter(tfliteModel)

            var colour = intent.getStringExtra("colour").toString().toLowerCase().replace(Regex("\\b[a-z]")) { it.value.capitalize() }
            var condition = intent.getStringExtra("condition").toString().toLowerCase().replace(Regex("\\b[a-z]")) { it.value.capitalize() }
            var make = intent.getStringExtra("make").toString().toLowerCase().replace(Regex("\\b[a-z]")) { it.value.capitalize() }
            var model = intent.getStringExtra("selected_model").toString()
            var mileage = intent.getStringExtra("mileage").toString().toInt()
            var yearOfManufacture = intent.getStringExtra("year").toString().toInt()

            val colours = listOf("Black", "Blue", "Grey", "Red", "Silver", "White")
            val conditions = listOf("Good Condition", "Perfect Condition", "Poor Condition", "Spares Or Repairs")
            val makes = resources.getStringArray(R.array.make_items_2).toList()
            val models = resources.getStringArray(R.array.model_items).toList()

            val colourEncoded = oneHotEncode(colour, colours)
            val conditionEncoded = oneHotEncode(condition, conditions)
            val makeEncoded = oneHotEncode(make, makes)
            val modelEncoded = oneHotEncode(model, models)

            val scaledMileage = standardize(mileage.toDouble(), 356243.1805, 171069.5643724576)
            val scaledYearOfManufacture = standardize(yearOfManufacture.toDouble(), 1987.391, 12.732875519693106)

            println("Colour encoded length: ${colourEncoded.size}")
            println("Condition encoded length: ${conditionEncoded.size}")
            println("Make encoded length: ${makeEncoded.size}")
            println("Model encoded length: ${modelEncoded.size}")


            val inputArray = floatArrayOf(
                *colourEncoded.copyOfRange(0, colourEncoded.size),
                *conditionEncoded.copyOfRange(0, conditionEncoded.size),
                *makeEncoded.copyOfRange(0, makeEncoded.size),
                *modelEncoded.copyOfRange(0, modelEncoded.size),
                scaledMileage,
                scaledYearOfManufacture
            )

            val inputTensor = tflite.getInputTensor(0)
            val inputSize = inputTensor.numBytes()

            val inputBuffer = ByteBuffer.allocateDirect(inputSize)
                .order(ByteOrder.nativeOrder())

            inputBuffer.asFloatBuffer().put(inputArray)

            val outputShape = tflite.getOutputTensor(0).shape()
            val outputDataType = tflite.getOutputTensor(0).dataType()
            val outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType)

            tflite.run(inputBuffer, outputBuffer.buffer)
            val outputArray = outputBuffer.floatArray

            predictedPrice = outputArray[0]

            val recommendPrice = findViewById<TextView>(R.id.recommendPrice)

            recommendPrice.text = "£"+ outputArray[0].toInt().toString()



        }

        selectModel.setOnClickListener {
            val intent = Intent(this@SellCar2, SelectModel::class.java)

            if (vehicleData?.make != null) {intent.putExtra("selected_make", vehicleData?.make)}
            if (vehicleData?.make == null) {
                val makePattern = Pattern.compile("Make: (.+)")
                val colourPattern = Pattern.compile("Colour: (.+)")
                val yearPattern = Pattern.compile("Year Of Manufacture: (.+)")
                val matcher = makePattern.matcher(textView.text)
                val matcher2 = colourPattern.matcher(textView.text)
                val matcher3 = yearPattern.matcher(textView.text)

                if (matcher.find()) {
                    val make = matcher.group(1)
                    intent.putExtra("selected_make", make)
                }

                if (matcher2.find()) {
                    val colour = matcher2.group(1)
                    intent.putExtra("colour", colour)
                }

                if (matcher3.find()) {
                    val year = matcher3.group(1)
                    intent.putExtra("year", year)
                }

            }

            val makePattern = Pattern.compile("Make: (.+)")
            val colourPattern = Pattern.compile("Colour: (.+)")
            val yearPattern = Pattern.compile("Year Of Manufacture: (.+)")

            val matcher = makePattern.matcher(textView.text)
            val matcher2 = colourPattern.matcher(textView.text)
            val matcher3 = yearPattern.matcher(textView.text)

            var makeREGX = ""
            var colourREGX = ""
            var yearREGX = ""

            if (matcher.find()) {
                makeREGX = matcher.group(1)
            }

            if (matcher2.find()) {
                colourREGX = matcher2.group(1)
            }

            if (matcher3.find()) {
                yearREGX = matcher3.group(1)
            }

            intent.putExtra("price", price)
            intent.putExtra("condition", condition)
            intent.putExtra("mileage", mileage)
            intent.putExtra("description", description)
            intent.putExtra("page", "sellCar")
            intent.putExtra("carText", textView.text)
            intent.putExtra("registration", vehicleData?.registrationNumber.toString())
            intent.putExtra("imageUri0", imageUri0)
            intent.putExtra("imageUri1", imageUri1)
            intent.putExtra("imageUri2", imageUri2)

            intent.putExtra("model", imageUri2)
            intent.putExtra("imageUri2", imageUri2)

            intent.putExtra("model", intent.getStringExtra("model").toString())
            intent.putExtra("condition", intent.getStringExtra("condition").toString())

            intent.putExtra("make", makeREGX)
            intent.putExtra("year", yearREGX)
            intent.putExtra("colour", colourREGX)

            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        CoroutineScope(Dispatchers.Main).launch {
            if (intent.getStringExtra("carText") == null) {
                vehicleData = fetchRegistrationData().await()
                updateTextView(vehicleData!!)
            } else {
                vehicleData = fetchRegistrationData().await()
                textView.text = intent.getStringExtra("carText")
            }
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.upload0-> {
                imageNumber = 0
                var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
            }
            R.id.upload1-> {
                imageNumber = 1
                var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
            }
            R.id.upload2-> {
                imageNumber = 2
                var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
            }
            R.id.postCar-> {
                val uploadStatus = uploadImage()
                if (uploadStatus == 1) {
                    addCar()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMAGE && data != null) {

            val image0 = findViewById<ImageView>(R.id.upload0)
            val image1 = findViewById<ImageView>(R.id.upload1)
            val image2 = findViewById<ImageView>(R.id.upload2)

            when (imageNumber) {
                0 -> {
                    imageUri0 = data?.data
                    image0.setImageURI(imageUri0)
                }
                1 -> {
                    imageUri1 = data?.data
                    image1.setImageURI(imageUri1)
                }
                2 -> {
                    imageUri2 = data?.data
                    image2.setImageURI(imageUri2)
                }
            }
        }
    }

    private fun classifyImage(bitmap: Bitmap): String {
        val imgWidth = img_width
        val imgHeight = img_height

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(224, 224))
            .add(ResizeOp(imgHeight, imgWidth, ResizeMethod.NEAREST_NEIGHBOR))
            .add(preprocessMobilenetV2())
            .build()

        val inputTensorImage = imageProcessor.process(TensorImage(DataType.FLOAT32).apply { load(bitmap) })

        val inputByteBuffer = inputTensorImage.buffer

        val prediction = runInference(inputByteBuffer)[0]

        println("PREDDDD"+prediction)

        return if (prediction > 0.975) {
            "Not Car"
        } else {
            "Car"
        }
    }


    private fun runInference(inputByteBuffer: ByteBuffer): FloatArray {
        val outputTensorBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)

        tflite.run(inputByteBuffer, outputTensorBuffer.buffer)

        return outputTensorBuffer.floatArray
    }


    private fun preprocessMobilenetV2(): TensorOperator {
        val mean = 0f
        val std = 255f
        return NormalizeOp(mean, std)
    }




    private fun initTfliteInterpreter(modelFile: String) {
        val options = Interpreter.Options()
        options.setNumThreads(4)
        tflite = Interpreter(loadModelFile(this, modelFile), options)
    }


    @Throws(IOException::class)
    private fun loadModelFile(sellCar2: SellCar2, modelFile: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assets.openFd("car_classifier_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }


    fun rotateImageIfNeeded(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        val exifInterface = inputStream?.let { ExifInterface(it) }
        val orientation =
            exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun compressAndResizeImage(uri: Uri?): ByteArray {
        if (uri == null) {
            throw IllegalArgumentException("Uri cannot be null")
        }

        val compressedImage = compressImage(uri, contentResolver)
        var bitmap = BitmapFactory.decodeByteArray(compressedImage, 0, compressedImage.size)

        bitmap = rotateImageIfNeeded(this, uri, bitmap)

        val resizedBitmap = resizeImage(bitmap, maxWidth = 800, maxHeight = 800)

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return outputStream.toByteArray()
    }


    private fun compressImage(uri: Uri, contentResolver: ContentResolver, quality: Int = 75): ByteArray {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    private fun resizeImage(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun uploadImage(): Int {
        val intent = intent
        val registration = intent.getStringExtra("registration")
        var storageRef = Firebase.storage.reference

        val carImage0 = storageRef.child("images/image0-$registration")
        val carImage1 = storageRef.child("images/image1-$registration")
        val carImage2 = storageRef.child("images/image2-$registration")

        val uris = arrayOf(imageUri0, imageUri1, imageUri2)
        val images = arrayOf(carImage0, carImage1, carImage2)

        val urlList = mutableListOf<String?>("", "", "")

        val successfulUploads = AtomicInteger(0)

        var classificationCount = 0


        loopLabel@ for (i in 0..2) {
            uris[i]?.let { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val classification = classifyImage(bitmap)
                println("classification"+ classification)

                if (classification == "Car") {
                    classificationCount += 1
                }


                if ((i == 2) && (classificationCount != 2)) {

                    val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val popupView = inflater.inflate(R.layout.error_window_layout, null)

                    val fadeInAnimation = AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_fade_in)
                    val fadeOutAnimation = AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_fade_out)

                    popupView.startAnimation(fadeInAnimation)

                    val showPopupButton = findViewById<LinearLayout>(R.id.linearLayout25)

                    val dimmedBackground = View(this)
                    dimmedBackground.layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    dimmedBackground.setBackgroundColor(Color.parseColor("#80000000"))

                    val frameLayout = FrameLayout(this)
                    frameLayout.layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                    frameLayout.addView(dimmedBackground)
                    frameLayout.addView(popupView)

                    val popupWindow = PopupWindow(frameLayout,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, true)

                    popupWindow.setBackgroundDrawable(BitmapDrawable())
                    popupWindow.showAtLocation(showPopupButton, Gravity.CENTER, 0, 0)

                    val dismissButton = popupView.findViewById<Button>(R.id.dismiss)

                    dismissButton.setOnClickListener {
                        popupView.startAnimation(fadeOutAnimation)

                        fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation) {}

                            override fun onAnimationEnd(animation: Animation) {
                                popupWindow.dismiss()
                            }

                            override fun onAnimationRepeat(animation: Animation) {}
                        })
                    }

                    dimmedBackground.setOnTouchListener { _, _ ->
                        popupView.startAnimation(fadeOutAnimation)

                        fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation) {}

                            override fun onAnimationEnd(animation: Animation) {
                                popupWindow.dismiss()
                            }
                            override fun onAnimationRepeat(animation: Animation) {}
                        })
                        true
                    }

                    return 0

                } else if ((i == 2) && (classificationCount == 2)) {
                    uploadImages(uris, images, successfulUploads)
                }
            }
        }
        return 1
    }

    private fun uploadImages(uris: Array<Uri?>, images: Array<StorageReference>, successfulUploads: AtomicInteger) {
        val pd = ProgressDialog(this@SellCar2)
        pd.setTitle("Posting Car...")
        pd.show()

        for (i in 0..2) {
            uris[i]?.let { uri ->
                val compressedAndResizedImage = compressAndResizeImage(uri)
                val uploadTask = images[i].putBytes(compressedAndResizedImage)

                uploadTask.addOnSuccessListener {
                    images[i].downloadUrl.addOnSuccessListener { _ ->
                        if (successfulUploads.incrementAndGet() == 3) {
                            val loggedInUser = application as Username
                            val user = loggedInUser.username
                            val database = Firebase.database.getReference("users")
                            database.child(user).get().addOnSuccessListener { dataSnapshot ->
                                if (dataSnapshot.exists()) {
                                    val address = dataSnapshot.child("address").value.toString()
                                    val intent = Intent(this, MainPage::class.java)
                                    intent.putExtra("address", address)
                                    startActivity(intent)
                                    overridePendingTransition(
                                        androidx.appcompat.R.anim.abc_fade_out,
                                        androidx.appcompat.R.anim.abc_fade_in
                                    )
                                    pd.dismiss()
                                    Toast.makeText(this, "Car Posted!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(
                            applicationContext,
                            "Error: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun addCar() {
        val databaseUsers = Firebase.database.getReference("users")
        val loggedInUser = application as Username
        val user = loggedInUser.username
        databaseUsers.child(user).get().addOnSuccessListener {
            if(it.exists()) {
                val address = it.child("address").value.toString()
                val intent = intent
                val databaseCars = Firebase.database.getReference("cars")

                val priceText = findViewById<EditText>(R.id.editTextCarPrice)

                val registration = vehicleData?.registrationNumber
                val make = vehicleData?.make
                val colour = vehicleData?.colour
                val fuelType = vehicleData?.fuelType
                val registrationYear = vehicleData?.registrationYear
                val taxDueDate = vehicleData?.taxDueDate
                val mileage = intent.getStringExtra("mileage")
                val description = intent.getStringExtra("description")
                val yearOfManufacture = vehicleData?.yearOfManufacture
                val price = priceText.text.toString()
                val model = selectModel.text.toString()
                val co2Emissions = vehicleData?.co2Emissions
                val engineCapacity = vehicleData?.engineCapacity
                val username = user
                val condition = intent.getStringExtra("condition")

                var priceType = ""

                val priceRatio = price.substring(1).toInt().toDouble() / predictedPrice

                val greatPriceUpperBound = 1.0
                val goodPriceUpperBound = 1.05
                val fairPriceUpperBound = 1.15

                priceType = when {
                    priceRatio <= greatPriceUpperBound -> "Great Price"
                    priceRatio <= goodPriceUpperBound -> "Good Price"
                    priceRatio <= fairPriceUpperBound -> "Fair Price"
                    else -> "Bad Price"
                }

                val car = Car(registration, make, colour, fuelType, registrationYear, taxDueDate, mileage.toString(), yearOfManufacture, price, model, user, address, condition, co2Emissions, engineCapacity, description.toString(), priceType)
                databaseCars.child(registration.toString()).setValue(car).addOnSuccessListener { println("DONE!!") }.addOnFailureListener { println("FAILED :(") }
            }
        }.addOnFailureListener{}
    }

    private fun updateTextView(vehicleData: VehicleData) {

        textView.text = "Registration: " + vehicleData.registrationNumber + "\nMake: " + vehicleData.make +
                "\nColour: " + vehicleData.colour + "\nFuel Type " + vehicleData.fuelType +
                "\nTax Due Date: " + vehicleData.taxDueDate + "\nYear Of Manufacture: " +
                vehicleData.yearOfManufacture

        pred_colour = vehicleData.colour
        pred_make = vehicleData.make
        pred_year = vehicleData.yearOfManufacture

        intent.putExtra("make", pred_make.toString())
        intent.putExtra("colour", pred_colour.toString())
        intent.putExtra("year", pred_year.toString())
    }


    private suspend fun fetchRegistrationData(): Deferred<VehicleData> = coroutineScope {
        val gson = GsonBuilder().create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://driver-vehicle-licensing.api.gov.uk/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(DVLAService::class.java)

        val intent = intent
        val reg = intent.getStringExtra("registration").toString()

        val payload = mapOf("registrationNumber" to reg)
        val apiKey = "QKL4mvJLbR2dU32AL6oRo4GAl89EZhzQ5omO6aXF"

        val vehicleDataDeferred = async { service.getVehicleData(apiKey, payload) }
        vehicleDataDeferred

    }

    fun switchToSellCar(view: View) {
        startActivity(Intent(this@SellCar2,SellCar::class.java))
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun switchToSellCar3(view: View) {
        val mileage = intent.getStringExtra("mileage").toString()
        val price = intent.getStringExtra("price").toString()
        val username = intent.getStringExtra("username").toString()
        val condition = intent.getStringExtra("condition").toString()

        val intent = Intent(this, SellCar3::class.java)
        intent.putExtra("registration", vehicleData?.registrationNumber)
        intent.putExtra("make", vehicleData?.make)
        intent.putExtra("colour", vehicleData?.colour)
        intent.putExtra("fuelType", vehicleData?.fuelType)
        intent.putExtra("registrationYear", vehicleData?.registrationYear)
        intent.putExtra("taxDueDate", vehicleData?.taxDueDate)
        intent.putExtra("mileage", mileage)
        intent.putExtra("yearOfManufacture", vehicleData?.yearOfManufacture)
        intent.putExtra("co2Emissions", vehicleData?.co2Emissions)
        intent.putExtra("engineCapacity", vehicleData?.engineCapacity)
        intent.putExtra("price", price)
        intent.putExtra("username", username)
        intent.putExtra("condition", condition)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    fun oneHotEncode(value: String, categories: List<String>): FloatArray {
        val index = categories.indexOf(value)
        val encoded = FloatArray(categories.size) { 0.0f }
        if (index >= 0 && index < categories.size) {
            encoded[index] = 1.0f
        }
        return encoded
    }

    fun standardize(value: Double, mean: Double, std: Double): Float {
        return ((value - mean) / std).toFloat()
    }



    fun loadModelFile2(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}