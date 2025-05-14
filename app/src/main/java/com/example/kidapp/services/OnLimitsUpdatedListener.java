package com.example.kidapp.services;

import com.example.kidapp.apps.AppLimit;

import java.util.ArrayList;

public interface OnLimitsUpdatedListener {
    void onLimitsUpdated(ArrayList<AppLimit> updatedLimits);
}
