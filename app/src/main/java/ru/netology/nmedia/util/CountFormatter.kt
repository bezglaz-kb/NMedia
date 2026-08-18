package ru.netology.nmedia.util


fun formatCount(count: Int): String {
    return when {
        count < 1_000 -> count.toString()
        count < 10_000 -> {
            val thousands = count / 1_000
            val hundreds = (count % 1_000) / 100
            if (hundreds == 0) "${thousands}K" else "${thousands}.${hundreds}K"
        }

        count < 1_000_000 -> {
            "${count / 1_000}K"
        }

        else -> {
            val millions = count / 1_000_000
            val hundredsOfThousands = (count % 1_000_000) / 100_000
            if (hundredsOfThousands == 0) "${millions}M" else "${millions}.${hundredsOfThousands}M"
        }
    }
}