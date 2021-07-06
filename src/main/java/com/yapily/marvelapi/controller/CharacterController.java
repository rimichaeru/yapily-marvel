package com.yapily.marvelapi.controller;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.yapily.marvelapi.entity.Character;
import com.yapily.marvelapi.helper.RequestHelper;
import com.yapily.marvelapi.repository.CharacterRepository;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@Api(tags = "Characters")
public class CharacterController {

    @Autowired
    private Environment env;

    @Autowired
    public CharacterRepository charRepo;

    // get all character ids
    @ApiOperation(value = "Get all the Marvel Character IDs")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Character IDs Populated"),
            @ApiResponse(code = 204, message = "No Content"),
    })
    @GetMapping("/characters")
    public ResponseEntity getAllCharacterIds() {
        List<Character> allCharacters = this.charRepo.findAll();

        // get Marvel IDs only
        List<Integer> allCharacterIds = allCharacters.stream().map(Character::getMarvelid).collect(Collectors.toList());

        if (allCharacterIds.size() == 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No Content");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(allCharacterIds);
        }
    }

    // get character information from id query
    @ApiOperation(value = "Get a Marvel Character's Name, Description and Image, and optionally translate the Description to another language (2-letter ISO-639-1 language code)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Character Found"),
            @ApiResponse(code = 400, message = "Incorrect Language Provided, please use ISO-639-1 language codes", response = Error.class),
            @ApiResponse(code = 404, message = "Character Not Found", response = Error.class),
            @ApiResponse(code = 511, message = "Google API Key Required", response = Error.class),
    })
    @GetMapping("/characters/{characterId}")
    public ResponseEntity getCharacter(@ApiParam(value = "characterId",
            required = true) @PathVariable int characterId, @ApiParam(value = "language",
            required = false, defaultValue = "en") @RequestParam(defaultValue = "en") String language) {
        // RequestParam options
        // @RequestParam(defaultValue = "en")
        // @RequestParam(required = false)

        String googleApiKey = env.getProperty("GOOGLE_API_KEY");

        try {
            List<Character> targetCharacter = this.charRepo.findByMarvelid(characterId);
            String descriptionFirstHalf = targetCharacter.get(0).getDescription();
            String descriptionSecondHalf = targetCharacter.get(0).getDescription_overflow();
            targetCharacter.get(0).setDescription(descriptionFirstHalf + descriptionSecondHalf);
            targetCharacter.get(0).setDescription_overflow("");

            if (!language.equals("en") && !targetCharacter.get(0).getDescription().equals("")) {
//              TRANSLATE
                if (googleApiKey.equals("KEY_HERE") || googleApiKey.equals("")) {
                    throw new ResponseStatusException(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED, "Google API Key Required");
                }

                String englishDesc = targetCharacter.get(0).getDescription();
                try {
                    Translate translate = TranslateOptions.newBuilder().setApiKey(googleApiKey).build().getService();
                    Translation translation = translate.translate(
                            englishDesc,
                            Translate.TranslateOption.sourceLanguage("en"),
                            Translate.TranslateOption.targetLanguage(language)
                    );

                    targetCharacter.get(0).setDescription(translation.getTranslatedText());
                    targetCharacter.get(0).setDescription_overflow(englishDesc);

                } catch (Exception exc) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect Language Provided, please use ISO-639-1 language codes", exc);
                }
            }

            return ResponseEntity.status(HttpStatus.OK).body(targetCharacter);

        } catch (IndexOutOfBoundsException exc) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Character Not Found", exc);
        }


    }

    @ApiOperation(value = "Update the H2 SQL Database with all of the Marvel Characters from the official API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Database Successfully Seeded, please GET /characters or /characters/MARVEL_ID"),
            @ApiResponse(code = 511, message = "Google API Key Required", response = Error.class),
    })
    @GetMapping("/characters/seed")
    public ResponseEntity seedCharacters() throws NoSuchAlgorithmException, IOException {

        String publicKey = env.getProperty("MARVEL_PUBLIC_KEY");
        String privateKey = env.getProperty("MARVEL_PRIVATE_KEY");

        if (publicKey.equals("KEY_HERE") || privateKey.equals("KEY_HERE") || publicKey.equals("") || privateKey.equals("")) {
            throw new ResponseStatusException(HttpStatus.NETWORK_AUTHENTICATION_REQUIRED, "Please Provide Public & Private Marvel API-KEYS");
        }

        // reset the SQL DB
        this.charRepo.deleteAll();
        this.charRepo.flush();

        // get total characters to determine how many times fetch needs to loop
        Long maxCharacters = new RequestHelper().queryMaxCharacters(publicKey, privateKey);
        int repeat = (int) (maxCharacters / 100) + 1;

        new RequestHelper().getApiCharacters(publicKey, privateKey, charRepo, 100, repeat);


        // For testing, so that not all the 1400+ characters are queried from the Marvel API
        // new RequestHelper().getApiCharacters(publicKey, privateKey, charRepo, 2, 5);

        return ResponseEntity.status(HttpStatus.OK).body("Database Successfully Seeded, please GET /characters or /characters/MARVEL_ID");

    }

}
