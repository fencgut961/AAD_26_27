package com.example.layared.archetype.service;

import com.example.layared.archetype.repository.CsvRepository;
import com.example.layared.archetype.repository.TxtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class ProcessFilesService {

    private final CsvRepository fileCsv;

    private final TxtRepository fileTxt;

//    Este constructor es necesario para la inyección de dependencias de Spring,
//    pero no se utiliza en este ejemplo ya que se sustituye por la anotación @RequiredArgsConstructor de Lombok,
//    que genera automáticamente un constructor con los campos finales.
//    public ProcessFilesService(final CsvReader csvReader, final TxtReader txtReader) {
//        this.csvReader = csvReader;
//        this.txtReader = txtReader;
//    }


    public void processFilesService() {

    }

    private String mergeFileProcess(File a, String b) {
        return null;
    }


}
