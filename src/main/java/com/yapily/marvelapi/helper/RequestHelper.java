package com.yapily.marvelapi.helper;

import com.yapily.marvelapi.entity.Character;
import com.yapily.marvelapi.entity.Thumbnail;
import com.yapily.marvelapi.repository.CharacterRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

public class RequestHelper {

    public Long queryMaxCharacters(String publicKey, String privateKey) throws NoSuchAlgorithmException, IOException {

        Long maxCharacters = null;

        // apikey = publicKey
        // ts = string time stamp
        String timeStamp = Instant.now().toString();
        // String timeStamp = "1";

        // hash = md5(ts+privateKey+publicKey)
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(timeStamp.getBytes(StandardCharsets.UTF_8));
        md.update(privateKey.getBytes(StandardCharsets.UTF_8));
        md.update(publicKey.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashText = bigInt.toString(16);
        while (hashText.length() < 32) {
            hashText = "0" + hashText;
        }

        // GET REQUEST
        URL url = new URL("https://gateway.marvel.com/v1/public/characters?apikey=" + publicKey + "&ts=" + timeStamp + "&hash=" + hashText + "&limit=1");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.connect();

        int responseCode = con.getResponseCode();

        if (responseCode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responseCode);
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            JSONParser jsonParser = new JSONParser();
            try {
                //Read JSON file
                Object obj = jsonParser.parse(content.toString());
                JSONObject responseObject = new JSONObject((Map) obj);

                JSONObject responseData = (JSONObject) responseObject.get("data");
                maxCharacters = (Long) responseData.get("total");


            } catch (ParseException pe) {

                System.out.println("position: " + pe.getPosition());
                System.out.println(pe);
            }

            in.close();
        }

        con.disconnect();

        return maxCharacters;
    }


    public void getApiCharacters(String publicKey, String privateKey, CharacterRepository charRepo, int characterAmount, int repeat) throws NoSuchAlgorithmException, IOException {

        // for loop based on amount of characters, determines offset (i*characterAmount) and ends at loop = maxCharacters/100 (repeat)

        for (int i = 0; i < repeat; i++) {

            // apikey = publicKey
            // ts = string time stamp
            String timeStamp = Instant.now().toString();
            // String timeStamp = "1";

            // hash = md5(ts+privateKey+publicKey)
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(timeStamp.getBytes(StandardCharsets.UTF_8));
            md.update(privateKey.getBytes(StandardCharsets.UTF_8));
            md.update(publicKey.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            String hashText = bigInt.toString(16);
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }

            // GET REQUEST
            URL url = new URL("https://gateway.marvel.com/v1/public/characters?apikey=" + publicKey + "&ts=" + timeStamp + "&hash=" + hashText + "&limit=" + characterAmount + "&offset=" + i * characterAmount);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                JSONParser jsonParser = new JSONParser();
                try {
                    //Read JSON file
                    Object obj = jsonParser.parse(content.toString());
                    JSONObject responseObject = new JSONObject((Map) obj);

                    JSONObject responseData = (JSONObject) responseObject.get("data");
                    JSONArray characterArray = (JSONArray) responseData.get("results");

                    characterArray.forEach(character -> {
                        JSONObject characterObject = (JSONObject) character;

                        System.out.println("ID: " + characterObject.get("id").toString());
                        System.out.println("NAME: " + characterObject.get("name").toString());

                        String newDescription;
                        String newDescriptionOverflow;
                        if (characterObject.get("description").toString().length() > 254) {
                            newDescription = characterObject.get("description").toString().substring(0, 254);
                            if (characterObject.get("description").toString().length() > 508) {
                                newDescriptionOverflow = characterObject.get("description").toString().substring(254, 508);
                            } else {
                                newDescriptionOverflow = characterObject.get("description").toString().substring(254);
                            }
                        } else {
                            newDescription = characterObject.get("description").toString();
                            newDescriptionOverflow = "";
                        }

                        JSONObject thumbnailObject = (JSONObject) characterObject.get("thumbnail");
                        Thumbnail newThumbnail = new Thumbnail(thumbnailObject.get("path").toString(), thumbnailObject.get("extension").toString());

                        Character newCharacter = new Character(Integer.parseInt(characterObject.get("id").toString()), characterObject.get("name").toString(), newDescription, newDescriptionOverflow, newThumbnail);

                        charRepo.save(newCharacter);

                    });

                } catch (ParseException pe) {

                    System.out.println("position: " + pe.getPosition());
                    System.out.println(pe);
                }

                in.close();
            }

            con.disconnect();
        }

    }

}
