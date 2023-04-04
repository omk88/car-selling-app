package com.example.carsellingandbuyingapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SelectModel : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_make)

        val selectedMinPrice = intent.getStringExtra("selected_minPrice")
        val selectedMaxPrice = intent.getStringExtra("selected_maxPrice")
        val selectedMinYear = intent.getStringExtra("selected_minYear")
        val selectedMaxYear = intent.getStringExtra("selected_maxYear")
        val selectedMinEmissions = intent.getStringExtra("selected_minEmissions")
        val selectedMaxEmissions = intent.getStringExtra("selected_maxEmissions")
        val selectedColour = intent.getStringExtra("selected_colour")

        val linearLayout = findViewById<LinearLayout>(R.id.verticalLinearLayout)

        val selectedMake = intent.getStringExtra("selected_make").toString()

        val items = when (selectedMake) {
            "ABARTH" -> resources.getStringArray(R.array.abarth_model_items)
            "AC" -> resources.getStringArray(R.array.ac_model_items)
            "AIXAM" -> resources.getStringArray(R.array.aixam_model_items)
            "ALPHA ROMEO" -> resources.getStringArray(R.array.alphaRomeo_model_items)
            "ALPINE" -> resources.getStringArray(R.array.alpine_model_items)
            "ARIEL" -> resources.getStringArray(R.array.ariel_model_items)
            "ASTON MARTIN" -> resources.getStringArray(R.array.astonMartin_model_items)
            "AUDI" -> resources.getStringArray(R.array.audi_model_items)
            "AUSTIN" -> resources.getStringArray(R.array.austin_model_items)
            "BENTLEY" -> resources.getStringArray(R.array.bentley_model_items)
            "BMW" -> resources.getStringArray(R.array.bmw_model_items)
            "BUGATTI" -> resources.getStringArray(R.array.bugatti_model_items)
            "CADILLAC" -> resources.getStringArray(R.array.cadillac_model_items)
            "CHEVROLET" -> resources.getStringArray(R.array.chevrolet_model_items)
            "CHRYSLER" -> resources.getStringArray(R.array.chrysler_model_items)
            "CITROEN" -> resources.getStringArray(R.array.citroen_model_items)
            "CORVETTE" -> resources.getStringArray(R.array.corvette_model_items)
            "CUPRA" -> resources.getStringArray(R.array.cupra_model_items)
            "DACIA" -> resources.getStringArray(R.array.dacia_model_items)
            "DAEWOO" -> resources.getStringArray(R.array.daewoo_model_items)
            "DAF" -> resources.getStringArray(R.array.daf_model_items)
            "DAIHATSU" -> resources.getStringArray(R.array.daihatsu_model_items)
            "DAIMLER" -> resources.getStringArray(R.array.daimler_model_items)
            "DATSUN" -> resources.getStringArray(R.array.datsun_model_items)
            "DE TOMASO" -> resources.getStringArray(R.array.deTomaso_model_items)
            "DFSK" -> resources.getStringArray(R.array.dfsk_model_items)
            "DODGE" -> resources.getStringArray(R.array.dodge_model_items)
            "DS AUTOMOBILES" -> resources.getStringArray(R.array.dsAutomobiles_model_items)
            "FERRARI" -> resources.getStringArray(R.array.ferrari_model_items)
            "FIAT" -> resources.getStringArray(R.array.fiat_model_items)
            "FORD" -> resources.getStringArray(R.array.ford_model_items)
            "GENESIS MOTOR" -> resources.getStringArray(R.array.genesisMotor_model_items)
            "GMC" -> resources.getStringArray(R.array.gmc_model_items)
            "GREAT WALL" -> resources.getStringArray(R.array.greatWall_model_items)
            "GWM ORA" -> resources.getStringArray(R.array.gwmOra_model_items)
            "HILLMAN" -> resources.getStringArray(R.array.hillman_model_items)
            "HOLDEN" -> resources.getStringArray(R.array.holden_model_items)
            "HONDA" -> resources.getStringArray(R.array.honda_model_items)
            "HUMMER" -> resources.getStringArray(R.array.hummer_model_items)
            "HYUNDAI" -> resources.getStringArray(R.array.hyundai_model_items)
            "INFINITI" -> resources.getStringArray(R.array.infiniti_model_items)
            "ISUZU" -> resources.getStringArray(R.array.isuzu_model_items)
            "JAGUAR" -> resources.getStringArray(R.array.jaguar_model_items)
            "JEEP" -> resources.getStringArray(R.array.jeep_model_items)
            "JENSEN" -> resources.getStringArray(R.array.jensen_model_items)
            "KIA" -> resources.getStringArray(R.array.kia_model_items)
            "LADA" -> resources.getStringArray(R.array.lada_model_items)
            "LAGONDA" -> resources.getStringArray(R.array.lagonda_model_items)
            "LAMBORGHINI" -> resources.getStringArray(R.array.lamborghini_model_items)
            "LAND ROVER" -> resources.getStringArray(R.array.landRover_model_items)
            "LEVC" -> resources.getStringArray(R.array.levc_model_items)
            "LEXUS" -> resources.getStringArray(R.array.lexus_model_items)
            "LINCOLN" -> resources.getStringArray(R.array.lincoln_model_items)
            "LOTUS" -> resources.getStringArray(R.array.lotus_model_items)
            "MASERATI" -> resources.getStringArray(R.array.maserati_model_items)
            "MAXUS" -> resources.getStringArray(R.array.maxus_model_items)
            "MAYBACH" -> resources.getStringArray(R.array.maybach_model_items)
            "MAZDA" -> resources.getStringArray(R.array.mazda_model_items)
            "MCLAREN" -> resources.getStringArray(R.array.mclaren_model_items)
            "MERCEDES-BENZ" -> resources.getStringArray(R.array.mercedesBenz_model_items)
            "MG" -> resources.getStringArray(R.array.mg_model_items)
            "MINI" -> resources.getStringArray(R.array.mini_model_items)
            "MITSUBISHI" -> resources.getStringArray(R.array.mitsubishi_model_items)
            "MOKE" -> resources.getStringArray(R.array.moke_model_items)
            "MORGAN" -> resources.getStringArray(R.array.morgan_model_items)
            "NISSAN" -> resources.getStringArray(R.array.nissan_model_items)
            "OPEL" -> resources.getStringArray(R.array.opel_model_items)
            "PANTHER" -> resources.getStringArray(R.array.panther_model_items)
            "PERODUA" -> resources.getStringArray(R.array.perodua_model_items)
            "PEUGEOT" -> resources.getStringArray(R.array.peugeot_model_items)
            "POLESTAR" -> resources.getStringArray(R.array.polestar_model_items)
            "PONTIAC" -> resources.getStringArray(R.array.pontiac_model_items)
            "PORSCHE" -> resources.getStringArray(R.array.porsche_model_items)
            "PROTON" -> resources.getStringArray(R.array.proton_model_items)
            "RADICAL" -> resources.getStringArray(R.array.radical_model_items)
            "RENAULT" -> resources.getStringArray(R.array.renault_model_items)
            "ROLLS-ROYCE" -> resources.getStringArray(R.array.rollsRoyce_model_items)
            "ROVER" -> resources.getStringArray(R.array.rover_model_items)
            "SAAB" -> resources.getStringArray(R.array.saab_model_items)
            "SEAT" -> resources.getStringArray(R.array.seat_model_items)
            "SHELBY" -> resources.getStringArray(R.array.shelby_model_items)
            "SKODA" -> resources.getStringArray(R.array.skoda_model_items)
            "SMART" -> resources.getStringArray(R.array.smart_model_items)
            "SSANGYONG" -> resources.getStringArray(R.array.ssangyong_model_items)
            "SUBARU" -> resources.getStringArray(R.array.subaru_model_items)
            "SUZUKI" -> resources.getStringArray(R.array.suzuki_model_items)
            "TESLA" -> resources.getStringArray(R.array.tesla_model_items)
            "TOYOTA" -> resources.getStringArray(R.array.toyota_model_items)
            "TRIUMPH" -> resources.getStringArray(R.array.triumph_model_items)
            "VAUXHALL" -> resources.getStringArray(R.array.vauxhall_model_items)
            "VOLKSWAGEN" -> resources.getStringArray(R.array.volkswagen_model_items)
            "VOLVO" -> resources.getStringArray(R.array.volvo_model_items)
            "WESTFIELD" -> resources.getStringArray(R.array.westfield_model_items)
            "ZIMMER" -> resources.getStringArray(R.array.zimmer_model_items)
            "NOBLE" -> resources.getStringArray(R.array.noble_model_items)
            else -> arrayOf<String>()
        }

        var previousFirstChar: Char? = null
        for (item in items) {
            val currentFirstChar = item[0].toUpperCase()

            if (previousFirstChar == null || currentFirstChar != previousFirstChar) {
                val divider = TextView(this)
                divider.text = currentFirstChar.toString()
                divider.textSize = 22f
                divider.setTextColor(Color.parseColor("#9A9A9A"))
                divider.setPadding(30, 7, 0, 7)

                divider.setBackgroundResource(R.drawable.divider_background)

                linearLayout.addView(divider)
            }

            val textView = TextView(this)
            textView.text = item
            textView.textSize = 16f
            textView.setPadding(0, 20, 0, 20)
            linearLayout.addView(textView)

            previousFirstChar = currentFirstChar

            textView.setOnClickListener {
                val intent = Intent(this@SelectModel, Search::class.java)
                intent.putExtra("selected_model", textView.text.toString())
                intent.putExtra("selected_make", selectedMake)
                intent.putExtra("selected_minEmissions", selectedMinEmissions)
                intent.putExtra("selected_maxEmissions", selectedMaxEmissions)
                intent.putExtra("selected_colour", selectedColour)
                intent.putExtra("selected_minPrice", selectedMinPrice)
                intent.putExtra("selected_maxPrice", selectedMaxPrice)
                intent.putExtra("selected_minYear", selectedMinYear)
                intent.putExtra("selected_maxYear", selectedMaxYear)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }
}