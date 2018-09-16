package com.cardoso.joao.pendencies;

import com.google.api.services.drive.model.File;

import java.util.HashSet;
import java.util.Set;

public class Atividade {
    String name;
    String createdTime;
    String modifiedTime;

    Atividade(String _name, String _createdTime, String _modifiedTime){
        name = _name;
        createdTime = _createdTime;
        modifiedTime = _modifiedTime;
    }
}
