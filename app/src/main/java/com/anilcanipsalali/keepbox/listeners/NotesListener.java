package com.anilcanipsalali.keepbox.listeners;

import com.anilcanipsalali.keepbox.model.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
