import os
import sys
import cv2
from google.cloud import vision
from pdf2image import convert_from_path
import tempfile
from PIL import Image

# ✅ Google credentials path
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "C:/Inkstall-Abhay/OCR/OCR/python/nextcloud-b4a5e-428cb90939d9.json"

# ✅ Poppler path
POPPLER_PATH = r"C:\Program Files\poppler-24.08.0\Library\bin"

# ✅ Image pre-processing: grayscale + binary threshold
def preprocess_image(image_path):
    img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
    _, thresh = cv2.threshold(img, 150, 255, cv2.THRESH_BINARY)
    preprocessed_path = image_path.replace(".png", "_preprocessed.png")
    cv2.imwrite(preprocessed_path, thresh)
    return preprocessed_path

# ✅ Google Vision OCR from image
def ocr_from_image(image_path, client):
    preprocessed_path = preprocess_image(image_path)

    with open(preprocessed_path, "rb") as image_file:
        content = image_file.read()

    image = vision.Image(content=content)

    # ✅ Language hint for better accuracy
    image_context = vision.ImageContext(language_hints=["en"])

    # ✅ OCR with context
    response = client.document_text_detection(image=image, image_context=image_context)

    if response.error.message:
        return f"API Error: {response.error.message}"

    return response.full_text_annotation.text.strip()

# ✅ OCR from PDF pages (converted to images)
def ocr_from_pdf(pdf_path, client):
    all_text = ""

    with tempfile.TemporaryDirectory() as temp_dir:
        images = convert_from_path(
            pdf_path,
            dpi=500,
            output_folder=temp_dir,
            poppler_path=POPPLER_PATH
        )

        for idx, image in enumerate(images):
            image_path = os.path.join(temp_dir, f"page_{idx}.png")
            image.save(image_path, "PNG")

            text = ocr_from_image(image_path, client)
            all_text += f"\n--- Page {idx+1} ---\n{text}"

    return all_text or "No text detected."

# ✅ Main method
def main(file_path):
    client = vision.ImageAnnotatorClient()

    try:
        ext = os.path.splitext(file_path)[1].lower()

        if ext == ".pdf":
            result = ocr_from_pdf(file_path, client)
        elif ext in [".png", ".jpg", ".jpeg"]:
            result = ocr_from_image(file_path, client)
        else:
            result = "Unsupported file type. Please upload a PDF or an image file."

        print(result)

    except Exception as e:
        print(f"Error: {str(e)}")

# ✅ Run script from CLI
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python ocr_script.py <file_path>")
    else:
        main(sys.argv[1])
