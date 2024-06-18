package me.metumortis.bounty

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException



    class Data(private val database: String) {
        private lateinit var publicConnection: Connection;
        fun connect(): Connection {
            var connection: Connection = DriverManager.getConnection("jdbc:sqlite:$database")
            this.publicConnection = connection;
            return connection;
        }
    }