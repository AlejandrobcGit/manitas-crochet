const addTransformation = (imageUrl, transformation) => {
    if (!imageUrl) return null;

    const url = new URL(imageUrl);

    // Ejemplo:
    // /8hlhxb9hx/carpeta/imagen.jpg
    const pathParts = url.pathname.split("/").filter(Boolean);

    if (pathParts.length < 2) {
        return imageUrl;
    }

    const imageKitId = pathParts[0];
    const imagePath = pathParts.slice(1).join("/");

    return `${url.origin}/${imageKitId}/${transformation}/${imagePath}`;
};

export const getCatalogImage = (imageUrl, width = 300) =>
    addTransformation(
        imageUrl,
        `tr:w-${width},q-auto,f-auto,l-image,i-BadgeIntagram.png,lx-N5,ly-N5,w-80,o-40,l-end`
    );

export const getGalleryImage = (imageUrl) =>
    addTransformation(
        imageUrl,
        "tr:w-1000,q-auto,f-auto,l-image,i-BadgeIntagram.png,lx-N10,ly-N10,w-150,o-70,l-end"
    );

export const getThumbnailImage = (imageUrl) =>
    addTransformation(
        imageUrl,
        "tr:w-150,h-150,fo-auto,q-80"
    );