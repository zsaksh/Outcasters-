import os
import shutil

models_dir = "app/build/intermediates/merged_assets/debug/out/models"
# Actually the models are stored in context.filesDir, which is in the emulator.
# I can't wipe them from here. But changing the DB version will reset their status to "not_installed".
