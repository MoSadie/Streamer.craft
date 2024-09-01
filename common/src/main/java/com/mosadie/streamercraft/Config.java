package com.mosadie.streamercraft;

public class Config {
    public String streamerBotIp;
    public int streamerBotPort;

    public static Config defaultConfig() {
        Config config = new Config();
        config.streamerBotIp = "127.0.0.1";
        config.streamerBotPort = 7474;
        return config;
    }
}
