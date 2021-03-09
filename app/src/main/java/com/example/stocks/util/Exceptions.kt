package com.example.stocks.util

import java.io.IOException

class ApiLimitException(message: String): IOException(message)
class ApiAccessException(message: String): IOException(message)
class OtherApiException(message: String): IOException(message)