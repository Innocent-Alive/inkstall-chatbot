import os
import sys
import io
from google.cloud import vision
from pdf2image import convert_from_path
import tempfile
from PIL import Image, ImageEnhance

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# Set path to Google credentials
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "C:/Inkstall-Abhay/OCR/OCR/python/nextcloud-b4a5e-428cb90939d9.json"

# Set path to Poppler (adjust this if your path is different)
POPPLER_PATH = r"C:\Program Files\poppler-24.08.0\Library\bin"

def enhance_image(image):
    image = image.convert("L")
    sharpener = ImageEnhance.Sharpness(image)
    image = sharpener.enhance(3.0)
    contrast = ImageEnhance.Contrast(image)
    image = contrast.enhance(1.5)
    # Commenting out resizing to avoid issues with large images
    # image = image.resize((image.width * 2, image.height * 2), Image.LANCZOS)
    return image

def ocr_from_image(image_path, client):
    try:
        with open(image_path, "rb") as image_file:
            content = image_file.read()

        if not content:
            return "Error: Image file is empty or corrupted."

        image = vision.Image(content=content)
        image_context = vision.ImageContext(language_hints=["en"])

        response = client.document_text_detection(image=image, image_context=image_context)

        if response.error.message:
            return f"API Error: {response.error.message}"

        return response.full_text_annotation.text.strip()

    except Exception as e:
        return f"Error reading image: {str(e)}"

def ocr_from_pdf(pdf_path, client):
    all_text = ""

    with tempfile.TemporaryDirectory() as temp_dir:
        print(f"[INFO] Temp directory created at: {temp_dir}")
        
        # Adjust DPI to 300 (instead of 500) to fix image data issues
        images = convert_from_path(
            pdf_path,
            dpi=300,  # Changed DPI to 300 for better compatibility
            output_folder=temp_dir,
            poppler_path=POPPLER_PATH
        )

        for idx, image in enumerate(images):
            print(f"[INFO] Processing page {idx + 1}")
            enhanced_image = enhance_image(image)
            image_path = os.path.join(temp_dir, f"page_{idx}.png")
            enhanced_image.save(image_path, "PNG")

            # Log image size for debugging
            print(f"[DEBUG] Image saved: {image_path} — Size: {os.path.getsize(image_path)} bytes")

            if os.path.getsize(image_path) < 1000:
                print(f"[ERROR] Image file {image_path} is too small. It might be corrupted.")
                return f"API Error: Image file {image_path} is too small."

            text = ocr_from_image(image_path, client)
            all_text += f"\n--- Page {idx+1} ---\n{text}"

    return all_text or "No text detected."

def main(file_path):
    client = vision.ImageAnnotatorClient()

    try:
        ext = os.path.splitext(file_path)[1].lower()

        if ext == ".pdf":
            result = ocr_from_pdf(file_path, client)
        elif ext in [".png", ".jpg", ".jpeg"]:
            image = Image.open(file_path)
            enhanced_image = enhance_image(image)

            with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp:
                enhanced_image.save(tmp.name)
                result = ocr_from_image(tmp.name, client)
        else:
            result = "Unsupported file type. Please upload a PDF or an image file."

        print("Text extracted successfully:\n")
        print(result)

    except Exception as e:
        print(f"[ERROR] Script Error: {str(e)}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python ocr_script.py <file_path>")
    else:
        main(sys.argv[1])
