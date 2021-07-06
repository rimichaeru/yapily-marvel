package com.yapily.marvelapi.repository;

import com.yapily.marvelapi.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterRepository extends JpaRepository<Character, Long> {

    List<Character> findByMarvelid(int marvelid);

}
