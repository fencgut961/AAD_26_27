package com.example.base.archetype.service;

import com.example.base.archetype.repository.CsvReader;
import com.example.base.archetype.repository.TxtReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.File;

@Repository
@RequiredArgsConstructor
public class ProcessFilesService {

    private final CsvReader csvReader;

    private final TxtReader txtReader;

//    public ProcessFilesService(final CsvReader csvReader, final TxtReader txtReader) {
//        this.csvReader = csvReader;
//        this.txtReader = txtReader;
//    }


    public void processFilesService() {
//        File a = csvReader.read();
//        String b = txtReader.read();
//        mergeFileProcess(a, b);
    }

    private String mergeFileProcess(File a, String b) {
        return null;
    }


}
