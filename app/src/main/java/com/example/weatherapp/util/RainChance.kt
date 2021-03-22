package com.example.weatherapp.util

class RainChance {
    companion object{
        fun getRainChance(data : Int) : String{
            if (data >= 85) return "mostly"
            else if (data < 85 && data >= 75) return "slightly"
            else return "no rain"
        }
    }
}